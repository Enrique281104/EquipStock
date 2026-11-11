package edu.sv.ues.dam235.equipstock.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.MantenimientoApi;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.database.EquipoDao;
import edu.sv.ues.dam235.equipstock.database.EquipoEntity;
import edu.sv.ues.dam235.equipstock.database.MantenimientoConDetalle;
import edu.sv.ues.dam235.equipstock.database.MantenimientoDao;
import edu.sv.ues.dam235.equipstock.database.MantenimientoEntity;
import edu.sv.ues.dam235.equipstock.models.MantenimientoDto;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.sync.Sincronizable;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.sync.SyncStatus;
import edu.sv.ues.dam235.equipstock.utils.FechaUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import retrofit2.Response;

public class MantenimientoRepository implements Sincronizable {

    private static MantenimientoRepository instance;

    public static synchronized MantenimientoRepository get(Context context) {
        if (instance == null) {
            instance = new MantenimientoRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final Context appContext;
    private final AppDatabase db;
    private final MantenimientoDao dao;
    private final EquipoDao equipoDao;
    private final MantenimientoApi api;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private MantenimientoRepository(Context appContext) {
        this.appContext = appContext;
        db = AppDatabase.get(appContext);
        dao = db.mantenimientoDao();
        equipoDao = db.equipoDao();
        api = ApiClient.get(appContext).create(MantenimientoApi.class);
    }

    // ---------- Lectura ----------

    /** estado: "" (todos), COMPLETADO, EN_PROCESO o VENCIDO. 0 = sin filtro en los ids. */
    public LiveData<List<MantenimientoConDetalle>> buscar(String texto, String estado,
                                                          long tecnicoId, long equipoLocalId) {
        return dao.buscar(texto, estado, tecnicoId, equipoLocalId, FechaUtil.hoy());
    }

    public LiveData<MantenimientoConDetalle> observarDetalle(long localId) {
        return dao.observarDetalle(localId);
    }

    public LiveData<List<MantenimientoEntity>> ultimosDeEquipo(long equipoLocalId, int limite) {
        return dao.ultimosDeEquipo(equipoLocalId, limite);
    }

    // ---------- Escritura local + intento de sincronizar ----------

    public void crear(long equipoLocalId, long tecnicoId, String tecnicoNombre, String tipo,
                      String estado, String fecha, String descripcion, String observaciones,
                      Resultado cb) {
        dbExecutor.execute(() -> {
            MantenimientoEntity m = new MantenimientoEntity();
            m.equipoLocalId = equipoLocalId;
            m.tecnicoId = tecnicoId;
            m.tecnicoNombre = tecnicoNombre;
            m.tipo = tipo;
            m.estado = estado;
            m.fechaProgramada = fecha;
            m.descripcion = descripcion;
            m.observaciones = observaciones;
            m.syncStatus = SyncStatus.PENDING_CREATE;
            dao.insertar(m);
            sincronizar(cb);
        });
    }

    public void editar(long localId, long equipoLocalId, long tecnicoId, String tecnicoNombre,
                       String tipo, String estado, String fecha, String descripcion,
                       String observaciones, Resultado cb) {
        dbExecutor.execute(() -> {
            enTransaccion(localId, m -> {
                m.equipoLocalId = equipoLocalId;
                m.tecnicoId = tecnicoId;
                m.tecnicoNombre = tecnicoNombre;
                m.tipo = tipo;
                m.estado = estado;
                m.fechaProgramada = fecha;
                m.descripcion = descripcion;
                m.observaciones = observaciones;
                m.version++;
                if (SyncStatus.SYNCED.equals(m.syncStatus)) {
                    m.syncStatus = SyncStatus.PENDING_UPDATE;
                }
            });
            sincronizar(cb);
        });
    }

    // ---------- Sincronización ----------

    public void sincronizar(Resultado callback) {
        SyncManager.get(appContext).sincronizar(callback);
    }

    @Override
    public String sincronizarAhora() throws IOException {
        String errorEnvio = enviarPendientes();
        String errorRecepcion = traerDelServidor();
        return errorEnvio != null ? errorEnvio : errorRecepcion;
    }

    private String enviarPendientes() throws IOException {
        String primerError = null;
        for (MantenimientoEntity m : dao.pendientes()) {
            String error;
            switch (m.syncStatus) {
                case SyncStatus.PENDING_CREATE: error = enviarCreacion(m); break;
                case SyncStatus.PENDING_UPDATE: error = enviarEdicion(m); break;
                default: error = null;
            }
            if (error != null && primerError == null) primerError = error;
        }
        return primerError;
    }

    private String enviarCreacion(MantenimientoEntity m) throws IOException {
        Long equipoId = serverIdEquipo(m.equipoLocalId);
        if (m.equipoLocalId != null && equipoId == null) return null;   // su equipo aún no llega

        Response<MantenimientoDto> r = api.crear(aDto(m, equipoId)).execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        MantenimientoDto creado = r.body();
        enTransaccion(m.localId, a -> {
            a.serverId = creado.getId();
            a.tecnicoId = creado.getTecnicoId();
            a.tecnicoNombre = creado.getTecnicoNombre();
            a.syncStatus = (a.version == m.version)
                    ? SyncStatus.SYNCED : SyncStatus.PENDING_UPDATE;
        });
        return null;
    }

    private String enviarEdicion(MantenimientoEntity m) throws IOException {
        Long equipoId = serverIdEquipo(m.equipoLocalId);
        if (m.equipoLocalId != null && equipoId == null) return null;

        Response<MantenimientoDto> r = api.actualizar(m.serverId, aDto(m, equipoId)).execute();
        if (r.code() == 404) {                        // ya no existe en el servidor
            enTransaccion(m.localId, a -> {
                a.serverId = null;
                a.syncStatus = SyncStatus.PENDING_CREATE;
            });
            return null;
        }
        if (!r.isSuccessful()) return mensajeHttp(r.code());

        enTransaccion(m.localId, a -> {
            if (a.version == m.version) a.syncStatus = SyncStatus.SYNCED;
        });
        return null;
    }

    private String traerDelServidor() throws IOException {
        Response<List<MantenimientoDto>> r = api.listar().execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        List<MantenimientoDto> remotos = r.body();
        Set<Long> idsRemotos = new HashSet<>();

        db.runInTransaction(() -> {
            for (MantenimientoDto d : remotos) {
                idsRemotos.add(d.getId());
                Long equipoLocal = localIdEquipo(d.getEquipoId());
                if (equipoLocal == null) continue;    // su equipo todavía no está en el teléfono

                MantenimientoEntity local = dao.porServerId(d.getId());
                if (local == null) {
                    MantenimientoEntity nuevo = new MantenimientoEntity();
                    copiar(d, nuevo, equipoLocal);
                    nuevo.serverId = d.getId();
                    nuevo.syncStatus = SyncStatus.SYNCED;
                    dao.insertar(nuevo);
                } else if (SyncStatus.SYNCED.equals(local.syncStatus)) {
                    copiar(d, local, equipoLocal);
                    dao.actualizar(local);
                }
                // si tiene cambios pendientes, gana lo local hasta que se envíe
            }
            for (MantenimientoEntity local : dao.sincronizados()) {
                if (!idsRemotos.contains(local.serverId)) dao.borrar(local.localId);
            }
        });
        return null;
    }

    // ---------- Utilidades ----------

    private Long serverIdEquipo(Long localId) {
        if (localId == null) return null;
        EquipoEntity e = equipoDao.porLocalId(localId);
        return e == null ? null : e.serverId;
    }

    private Long localIdEquipo(Long serverId) {
        if (serverId == null) return null;
        EquipoEntity e = equipoDao.porServerId(serverId);
        return e == null ? null : e.localId;
    }

    private boolean enTransaccion(long localId, Consumer<MantenimientoEntity> cambio) {
        final boolean[] existe = {false};
        db.runInTransaction(() -> {
            MantenimientoEntity actual = dao.porLocalId(localId);
            if (actual != null) {
                cambio.accept(actual);
                dao.actualizar(actual);
                existe[0] = true;
            }
        });
        return existe[0];
    }

    private MantenimientoDto aDto(MantenimientoEntity m, Long equipoId) {
        return new MantenimientoDto(equipoId, m.tecnicoId, m.tipo, m.estado,
                m.fechaProgramada, m.descripcion, m.observaciones);
    }

    private void copiar(MantenimientoDto d, MantenimientoEntity e, Long equipoLocalId) {
        e.equipoLocalId = equipoLocalId;
        e.tecnicoId = d.getTecnicoId();
        e.tecnicoNombre = d.getTecnicoNombre();
        e.tipo = d.getTipo();
        e.estado = d.getEstado();
        e.fechaProgramada = d.getFechaProgramada();
        e.descripcion = d.getDescripcion();
        e.observaciones = d.getObservaciones();
    }

    private String mensajeHttp(int codigo) {
        if (codigo == 401 || codigo == 403) {
            return "No tienes permiso para esta acción o tu sesión expiró.";
        }
        return "Error del servidor (" + codigo + ").";
    }

    public void cambiarEstado(long localId, String estado, Resultado cb) {
        dbExecutor.execute(() -> {
            enTransaccion(localId, m -> {
                m.estado = estado;
                m.version++;
                if (SyncStatus.SYNCED.equals(m.syncStatus)) {
                    m.syncStatus = SyncStatus.PENDING_UPDATE;
                }
            });
            sincronizar(cb);
        });
    }

}