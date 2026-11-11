package edu.sv.ues.dam235.equipstock.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.EquipoApi;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.database.CategoriaDao;
import edu.sv.ues.dam235.equipstock.database.CategoriaEntity;
import edu.sv.ues.dam235.equipstock.database.EquipoConDetalle;
import edu.sv.ues.dam235.equipstock.database.EquipoDao;
import edu.sv.ues.dam235.equipstock.database.EquipoEntity;
import edu.sv.ues.dam235.equipstock.database.UbicacionDao;
import edu.sv.ues.dam235.equipstock.database.UbicacionEntity;
import edu.sv.ues.dam235.equipstock.models.EquipoDto;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.sync.Sincronizable;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.sync.SyncStatus;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import retrofit2.Response;

public class EquipoRepository implements Sincronizable {

    private static EquipoRepository instance;

    public static synchronized EquipoRepository get(Context context) {
        if (instance == null) {
            instance = new EquipoRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final Context appContext;
    private final AppDatabase db;
    private final EquipoDao dao;
    private final CategoriaDao categoriaDao;
    private final UbicacionDao ubicacionDao;
    private final EquipoApi api;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private final Handler main = new Handler(Looper.getMainLooper());

    private EquipoRepository(Context appContext) {
        this.appContext = appContext;
        db = AppDatabase.get(appContext);
        dao = db.equipoDao();
        categoriaDao = db.categoriaDao();
        ubicacionDao = db.ubicacionDao();
        api = ApiClient.get(appContext).create(EquipoApi.class);
    }

    // ---------- Lectura ----------

    public LiveData<List<EquipoConDetalle>> buscar(String texto, String estado) {
        return dao.buscar(texto, estado);
    }

    public LiveData<EquipoConDetalle> buscarExacto(String texto) {
        return dao.buscarExacto(texto);
    }

    public LiveData<EquipoConDetalle> observarDetalle(long localId) {
        return dao.observarDetalle(localId);
    }

    public interface AlValidar {
        void resultado(boolean codigoRepetido, boolean serieRepetida);
    }

    // Revisa en el teléfono si el código o la serie ya existen, para avisar antes de guardar
    public void validarDuplicados(long excluirLocalId, String serie, String codigo, AlValidar cb) {
        dbExecutor.execute(() -> {
            boolean codigoRepetido = dao.contarCodigo(codigo, excluirLocalId) > 0;
            boolean serieRepetida = dao.contarSerie(serie, excluirLocalId) > 0;
            main.post(() -> cb.resultado(codigoRepetido, serieRepetida));
        });
    }

    // ---------- Escritura local + intento de sincronizar ----------

    public void crear(String nombre, String serie, String codigo, String estado,
                      long categoriaLocalId, long ubicacionLocalId, Resultado cb) {
        dbExecutor.execute(() -> {
            EquipoEntity e = new EquipoEntity();
            e.nombre = nombre;
            e.numeroSerie = serie;
            e.codigoActivo = codigo;
            e.estado = estado;
            e.categoriaLocalId = categoriaLocalId;
            e.ubicacionLocalId = ubicacionLocalId;
            e.syncStatus = SyncStatus.PENDING_CREATE;
            db.runInTransaction(() -> {
                dao.insertar(e);
                recalcularConteos();
            });
            sincronizar(cb);
        });
    }

    public void editar(long localId, String nombre, String serie, String codigo, String estado,
                       long categoriaLocalId, long ubicacionLocalId, Resultado cb) {
        dbExecutor.execute(() -> {
            enTransaccion(localId, e -> {
                e.nombre = nombre;
                e.numeroSerie = serie;
                e.codigoActivo = codigo;
                e.estado = estado;
                e.categoriaLocalId = categoriaLocalId;
                e.ubicacionLocalId = ubicacionLocalId;
                e.version++;
                if (SyncStatus.SYNCED.equals(e.syncStatus)) {
                    e.syncStatus = SyncStatus.PENDING_UPDATE;
                }
            });
            recalcularConteos();
            sincronizar(cb);
        });
    }

    public void eliminar(long localId, Resultado cb) {
        dbExecutor.execute(() -> {
            db.runInTransaction(() -> {
                EquipoEntity e = dao.porLocalId(localId);
                if (e == null) return;
                if (e.serverId == null) {
                    dao.borrar(localId);          // nunca llegó al servidor
                } else {
                    e.syncStatus = SyncStatus.PENDING_DELETE;
                    e.version++;
                    dao.actualizar(e);
                }
                recalcularConteos();
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
        for (EquipoEntity e : dao.pendientes()) {
            String error;
            switch (e.syncStatus) {
                case SyncStatus.PENDING_CREATE: error = enviarCreacion(e); break;
                case SyncStatus.PENDING_UPDATE: error = enviarEdicion(e); break;
                case SyncStatus.PENDING_DELETE: error = enviarBorrado(e); break;
                default: error = null;
            }
            if (error != null && primerError == null) primerError = error;
        }
        return primerError;
    }

    private String enviarCreacion(EquipoEntity e) throws IOException {
        Long categoriaId = serverIdCategoria(e.categoriaLocalId);
        Long ubicacionId = serverIdUbicacion(e.ubicacionLocalId);
        if (faltaReferencia(e, categoriaId, ubicacionId)) return null;   // se reintenta luego

        Response<EquipoDto> r = api.crear(aDto(e, categoriaId, ubicacionId)).execute();
        if (r.code() == 409) {
            // el servidor manda: se descarta el equipo local y se avisa
            db.runInTransaction(() -> {
                dao.borrar(e.localId);
                recalcularConteos();
            });
            return "No se pudo registrar \"" + e.nombre
                    + "\": el código de activo o el número de serie ya existen.";
        }
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        EquipoDto creado = r.body();
        boolean existe = enTransaccion(e.localId, a -> {
            a.serverId = creado.getId();
            a.syncStatus = (a.version == e.version)
                    ? SyncStatus.SYNCED : SyncStatus.PENDING_UPDATE;
        });
        if (!existe) {
            api.eliminar(creado.getId()).execute();   // lo borraron mientras se enviaba
        }
        return null;
    }

    private String enviarEdicion(EquipoEntity e) throws IOException {
        Long categoriaId = serverIdCategoria(e.categoriaLocalId);
        Long ubicacionId = serverIdUbicacion(e.ubicacionLocalId);
        if (faltaReferencia(e, categoriaId, ubicacionId)) return null;

        Response<EquipoDto> r = api.actualizar(e.serverId, aDto(e, categoriaId, ubicacionId))
                .execute();
        if (r.code() == 404) {                        // ya no existe en el servidor
            enTransaccion(e.localId, a -> {
                a.serverId = null;
                a.syncStatus = SyncStatus.PENDING_CREATE;
            });
            return null;
        }
        if (r.code() == 409) {
            // el servidor manda: se descarta el cambio y la bajada restaura sus datos
            enTransaccion(e.localId, a -> a.syncStatus = SyncStatus.SYNCED);
            return "No se pudieron guardar los cambios de \"" + e.nombre
                    + "\": el código de activo o el número de serie ya existen. "
                    + "Se restauraron los datos del servidor.";
        }
        if (!r.isSuccessful()) return mensajeHttp(r.code());

        enTransaccion(e.localId, a -> {
            if (a.version == e.version) a.syncStatus = SyncStatus.SYNCED;
        });
        return null;
    }

    private String enviarBorrado(EquipoEntity e) throws IOException {
        Response<Void> r = api.eliminar(e.serverId).execute();
        if (r.isSuccessful() || r.code() == 404) {
            db.runInTransaction(() -> {
                dao.borrar(e.localId);
                recalcularConteos();
            });
            return null;
        }
        if (r.code() == 409) {                        // tiene mantenimientos registrados
            enTransaccion(e.localId, a -> a.syncStatus = SyncStatus.SYNCED);
            recalcularConteos();
            return "No se puede eliminar \"" + e.nombre
                    + "\": tiene mantenimientos registrados. Márcalo como Inactivo.";
        }
        return mensajeHttp(r.code());
    }

    private String traerDelServidor() throws IOException {
        Response<List<EquipoDto>> r = api.listar().execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        List<EquipoDto> remotos = r.body();
        Set<Long> idsRemotos = new HashSet<>();

        db.runInTransaction(() -> {
            for (EquipoDto d : remotos) {
                idsRemotos.add(d.getId());
                EquipoEntity local = dao.porServerId(d.getId());
                if (local == null) {
                    EquipoEntity nuevo = new EquipoEntity();
                    copiar(d, nuevo);
                    nuevo.serverId = d.getId();
                    nuevo.syncStatus = SyncStatus.SYNCED;
                    dao.insertar(nuevo);
                } else if (SyncStatus.SYNCED.equals(local.syncStatus)) {
                    copiar(d, local);
                    dao.actualizar(local);
                }
                // si tiene cambios pendientes, gana lo local hasta que se envíe
            }
            for (EquipoEntity local : dao.sincronizadas()) {
                if (!idsRemotos.contains(local.serverId)) dao.borrar(local.localId);
            }
            recalcularConteos();
        });
        return null;
    }

    // ---------- Utilidades ----------

    private void recalcularConteos() {
        categoriaDao.recalcularConteo();
        ubicacionDao.recalcularConteo();
    }

    private Long serverIdCategoria(Long localId) {
        if (localId == null) return null;
        CategoriaEntity c = categoriaDao.porLocalId(localId);
        return c == null ? null : c.serverId;
    }

    private Long serverIdUbicacion(Long localId) {
        if (localId == null) return null;
        UbicacionEntity u = ubicacionDao.porLocalId(localId);
        return u == null ? null : u.serverId;
    }

    private Long localIdCategoria(Long serverId) {
        if (serverId == null) return null;
        CategoriaEntity c = categoriaDao.porServerId(serverId);
        return c == null ? null : c.localId;
    }

    private Long localIdUbicacion(Long serverId) {
        if (serverId == null) return null;
        UbicacionEntity u = ubicacionDao.porServerId(serverId);
        return u == null ? null : u.localId;
    }

    /** True si el equipo apunta a una categoría o ubicación que aún no llega al servidor. */
    private boolean faltaReferencia(EquipoEntity e, Long categoriaId, Long ubicacionId) {
        return (e.categoriaLocalId != null && categoriaId == null)
                || (e.ubicacionLocalId != null && ubicacionId == null);
    }

    private boolean enTransaccion(long localId, Consumer<EquipoEntity> cambio) {
        final boolean[] existe = {false};
        db.runInTransaction(() -> {
            EquipoEntity actual = dao.porLocalId(localId);
            if (actual != null) {
                cambio.accept(actual);
                dao.actualizar(actual);
                existe[0] = true;
            }
        });
        return existe[0];
    }

    private EquipoDto aDto(EquipoEntity e, Long categoriaId, Long ubicacionId) {
        return new EquipoDto(e.nombre, e.numeroSerie, e.codigoActivo, e.estado,
                categoriaId, ubicacionId);
    }

    private void copiar(EquipoDto d, EquipoEntity e) {
        e.nombre = d.getNombre();
        e.numeroSerie = d.getNumeroSerie();
        e.codigoActivo = d.getCodigoActivo();
        e.estado = d.getEstado();
        e.categoriaLocalId = localIdCategoria(d.getCategoriaId());
        e.ubicacionLocalId = localIdUbicacion(d.getUbicacionId());
    }

    private String mensajeHttp(int codigo) {
        if (codigo == 401 || codigo == 403) {
            return "No tienes permiso para esta acción o tu sesión expiró.";
        }
        return "Error del servidor (" + codigo + ").";
    }
}