package edu.sv.ues.dam235.equipstock.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.UbicacionApi;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.database.UbicacionDao;
import edu.sv.ues.dam235.equipstock.database.UbicacionEntity;
import edu.sv.ues.dam235.equipstock.models.UbicacionDto;
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

public class UbicacionRepository implements Sincronizable {

    private static UbicacionRepository instance;

    public static synchronized UbicacionRepository get(Context context) {
        if (instance == null) {
            instance = new UbicacionRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final Context appContext;
    private final AppDatabase db;
    private final UbicacionDao dao;
    private final UbicacionApi api;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private UbicacionRepository(Context appContext) {
        this.appContext = appContext;
        db = AppDatabase.get(appContext);
        dao = db.ubicacionDao();
        api = ApiClient.get(appContext).create(UbicacionApi.class);
    }

    // ---------- Lectura (siempre desde el teléfono) ----------

    public LiveData<List<UbicacionEntity>> buscar(String texto) {
        return dao.buscar(texto);
    }

    // ---------- Escritura local (instantánea) + intento de sincronizar ----------

    public void crear(String nombre, String descripcion, Resultado cb) {
        dbExecutor.execute(() -> {
            UbicacionEntity u = new UbicacionEntity();
            u.nombre = nombre;
            u.descripcion = descripcion;
            u.syncStatus = SyncStatus.PENDING_CREATE;
            dao.insertar(u);
            sincronizar(cb);
        });
    }

    public void editar(long localId, String nombre, String descripcion, Resultado cb) {
        dbExecutor.execute(() -> {
            enTransaccion(localId, u -> {
                u.nombre = nombre;
                u.descripcion = descripcion;
                u.version++;
                if (SyncStatus.SYNCED.equals(u.syncStatus)) {
                    u.syncStatus = SyncStatus.PENDING_UPDATE;
                }
            });
            sincronizar(cb);
        });
    }

    public void eliminar(long localId, Resultado cb) {
        dbExecutor.execute(() -> {
            db.runInTransaction(() -> {
                UbicacionEntity u = dao.porLocalId(localId);
                if (u == null) return;
                if (u.serverId == null) {
                    dao.borrar(localId);          // nunca llegó al servidor
                } else {
                    u.syncStatus = SyncStatus.PENDING_DELETE;
                    u.version++;
                    dao.actualizar(u);
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
        for (UbicacionEntity u : dao.pendientes()) {
            String error;
            switch (u.syncStatus) {
                case SyncStatus.PENDING_CREATE: error = enviarCreacion(u); break;
                case SyncStatus.PENDING_UPDATE: error = enviarEdicion(u); break;
                case SyncStatus.PENDING_DELETE: error = enviarBorrado(u); break;
                default: error = null;
            }
            if (error != null && primerError == null) primerError = error;
        }
        return primerError;
    }

    private String enviarCreacion(UbicacionEntity u) throws IOException {
        Response<UbicacionDto> r = api.crear(aDto(u)).execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        UbicacionDto creada = r.body();
        boolean existe = enTransaccion(u.localId, a -> {
            a.serverId = creada.getId();
            a.equiposVinculados = (int) creada.getEquiposVinculados();
            a.syncStatus = (a.version == u.version)
                    ? SyncStatus.SYNCED : SyncStatus.PENDING_UPDATE;
        });
        if (!existe) {
            api.eliminar(creada.getId()).execute();   // la borraron mientras se enviaba
        }
        return null;
    }

    private String enviarEdicion(UbicacionEntity u) throws IOException {
        Response<UbicacionDto> r = api.actualizar(u.serverId, aDto(u)).execute();
        if (r.code() == 404) {                        // ya no existe en el servidor
            enTransaccion(u.localId, a -> {
                a.serverId = null;
                a.syncStatus = SyncStatus.PENDING_CREATE;
            });
            return null;
        }
        if (!r.isSuccessful()) return mensajeHttp(r.code());

        enTransaccion(u.localId, a -> {
            if (a.version == u.version) a.syncStatus = SyncStatus.SYNCED;
        });
        return null;
    }

    private String enviarBorrado(UbicacionEntity u) throws IOException {
        Response<Void> r = api.eliminar(u.serverId).execute();
        if (r.isSuccessful() || r.code() == 404) {
            dao.borrar(u.localId);
            return null;
        }
        if (r.code() == 409) {                        // tiene equipos asignados
            enTransaccion(u.localId, a -> a.syncStatus = SyncStatus.SYNCED);
            return "No se puede eliminar \"" + u.nombre + "\": tiene equipos asignados.";
        }
        return mensajeHttp(r.code());
    }

    private String traerDelServidor() throws IOException {
        Response<List<UbicacionDto>> r = api.listar().execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        List<UbicacionDto> remotas = r.body();
        Set<Long> idsRemotos = new HashSet<>();

        db.runInTransaction(() -> {
            for (UbicacionDto d : remotas) {
                idsRemotos.add(d.getId());
                UbicacionEntity local = dao.porServerId(d.getId());
                if (local == null) {
                    UbicacionEntity nueva = new UbicacionEntity();
                    copiar(d, nueva);
                    nueva.serverId = d.getId();
                    nueva.syncStatus = SyncStatus.SYNCED;
                    dao.insertar(nueva);
                } else if (SyncStatus.SYNCED.equals(local.syncStatus)) {
                    copiar(d, local);
                    dao.actualizar(local);
                }
                // si tiene cambios pendientes, gana lo local hasta que se envíe
            }
            for (UbicacionEntity local : dao.sincronizadas()) {
                if (!idsRemotos.contains(local.serverId)) dao.borrar(local.localId);
            }
        });
        return null;
    }

    // ---------- Utilidades ----------

    private boolean enTransaccion(long localId, Consumer<UbicacionEntity> cambio) {
        final boolean[] existe = {false};
        db.runInTransaction(() -> {
            UbicacionEntity actual = dao.porLocalId(localId);
            if (actual != null) {
                cambio.accept(actual);
                dao.actualizar(actual);
                existe[0] = true;
            }
        });
        return existe[0];
    }

    private UbicacionDto aDto(UbicacionEntity u) {
        return new UbicacionDto(u.nombre, u.descripcion);
    }

    private void copiar(UbicacionDto d, UbicacionEntity e) {
        e.nombre = d.getNombre();
        e.descripcion = d.getDescripcion();
        e.equiposVinculados = (int) d.getEquiposVinculados();
    }

    private String mensajeHttp(int codigo) {
        if (codigo == 401 || codigo == 403) {
            return "No tienes permiso para esta acción o tu sesión expiró.";
        }
        return "Error del servidor (" + codigo + ").";
    }
}