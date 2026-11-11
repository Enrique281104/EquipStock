package edu.sv.ues.dam235.equipstock.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.CategoriaApi;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.database.CategoriaDao;
import edu.sv.ues.dam235.equipstock.database.CategoriaEntity;
import edu.sv.ues.dam235.equipstock.models.CategoriaDto;
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



public class CategoriaRepository implements Sincronizable {

    private static CategoriaRepository instance;

    public static synchronized CategoriaRepository get(Context context) {
        if (instance == null) {
            instance = new CategoriaRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final Context appContext;
    private final AppDatabase db;
    private final CategoriaDao dao;
    private final CategoriaApi api;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();



    private CategoriaRepository(Context appContext) {
        this.appContext = appContext;
        db = AppDatabase.get(appContext);
        dao = db.categoriaDao();
        api = ApiClient.get(appContext).create(CategoriaApi.class);
    }

    // ---------- Lectura (siempre desde el teléfono) ----------

    public LiveData<List<CategoriaEntity>> buscar(String texto) {
        return dao.buscar(texto);
    }

    public LiveData<List<CategoriaEntity>> todas() {
        return dao.todas();
    }

    // ---------- Escritura local (instantánea) + intento de sincronizar ----------

    public void crear(String nombre, String descripcion, String icono, Resultado cb) {
        dbExecutor.execute(() -> {
            CategoriaEntity c = new CategoriaEntity();
            c.nombre = nombre;
            c.descripcion = descripcion;
            c.icono = icono;
            c.syncStatus = SyncStatus.PENDING_CREATE;
            dao.insertar(c);
            sincronizar(cb);
        });
    }

    public void editar(long localId, String nombre, String descripcion, String icono, Resultado cb) {
        dbExecutor.execute(() -> {
            enTransaccion(localId, c -> {
                c.nombre = nombre;
                c.descripcion = descripcion;
                c.icono = icono;
                c.version++;
                if (SyncStatus.SYNCED.equals(c.syncStatus)) {
                    c.syncStatus = SyncStatus.PENDING_UPDATE;
                }
            });
            sincronizar(cb);
        });
    }

    public void eliminar(long localId, Resultado cb) {
        dbExecutor.execute(() -> {
            db.runInTransaction(() -> {
                CategoriaEntity c = dao.porLocalId(localId);
                if (c == null) return;
                if (c.serverId == null) {
                    dao.borrar(localId);          // nunca llegó al servidor
                } else {
                    c.syncStatus = SyncStatus.PENDING_DELETE;
                    c.version++;
                    dao.actualizar(c);
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
        for (CategoriaEntity c : dao.pendientes()) {
            String error;
            switch (c.syncStatus) {
                case SyncStatus.PENDING_CREATE: error = enviarCreacion(c); break;
                case SyncStatus.PENDING_UPDATE: error = enviarEdicion(c); break;
                case SyncStatus.PENDING_DELETE: error = enviarBorrado(c); break;
                default: error = null;
            }
            if (error != null && primerError == null) primerError = error;
        }
        return primerError;
    }

    private String enviarCreacion(CategoriaEntity c) throws IOException {
        Response<CategoriaDto> r = api.crear(aDto(c)).execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        CategoriaDto creada = r.body();
        boolean existe = enTransaccion(c.localId, a -> {
            a.serverId = creada.getId();
            a.equiposVinculados = (int) creada.getEquiposVinculados();
            // si el usuario la editó mientras viajaba, queda pendiente de actualizar
            a.syncStatus = (a.version == c.version)
                    ? SyncStatus.SYNCED : SyncStatus.PENDING_UPDATE;
        });
        if (!existe) {
            api.eliminar(creada.getId()).execute();   // la borraron mientras se enviaba
        }
        return null;
    }

    private String enviarEdicion(CategoriaEntity c) throws IOException {
        Response<CategoriaDto> r = api.actualizar(c.serverId, aDto(c)).execute();
        if (r.code() == 404) {                        // ya no existe en el servidor
            enTransaccion(c.localId, a -> {
                a.serverId = null;
                a.syncStatus = SyncStatus.PENDING_CREATE;
            });
            return null;
        }
        if (!r.isSuccessful()) return mensajeHttp(r.code());

        enTransaccion(c.localId, a -> {
            if (a.version == c.version) a.syncStatus = SyncStatus.SYNCED;
        });
        return null;
    }

    private String enviarBorrado(CategoriaEntity c) throws IOException {
        Response<Void> r = api.eliminar(c.serverId).execute();
        if (r.isSuccessful() || r.code() == 404) {
            dao.borrar(c.localId);
            return null;
        }
        if (r.code() == 409) {                        // tiene equipos vinculados
            enTransaccion(c.localId, a -> a.syncStatus = SyncStatus.SYNCED);
            return "No se puede eliminar \"" + c.nombre + "\": tiene equipos vinculados.";
        }
        return mensajeHttp(r.code());
    }

    private String traerDelServidor() throws IOException {
        Response<List<CategoriaDto>> r = api.listar().execute();
        if (!r.isSuccessful() || r.body() == null) return mensajeHttp(r.code());

        List<CategoriaDto> remotas = r.body();
        Set<Long> idsRemotos = new HashSet<>();

        db.runInTransaction(() -> {
            for (CategoriaDto d : remotas) {
                idsRemotos.add(d.getId());
                CategoriaEntity local = dao.porServerId(d.getId());
                if (local == null) {
                    CategoriaEntity nueva = new CategoriaEntity();
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
            // lo que ya no está en el servidor se quita del teléfono
            for (CategoriaEntity local : dao.sincronizadas()) {
                if (!idsRemotos.contains(local.serverId)) dao.borrar(local.localId);
            }
        });
        return null;
    }

    // ---------- Utilidades ----------

    private boolean enTransaccion(long localId, Consumer<CategoriaEntity> cambio) {
        final boolean[] existe = {false};
        db.runInTransaction(() -> {
            CategoriaEntity actual = dao.porLocalId(localId);
            if (actual != null) {
                cambio.accept(actual);
                dao.actualizar(actual);
                existe[0] = true;
            }
        });
        return existe[0];
    }

    private CategoriaDto aDto(CategoriaEntity c) {
        return new CategoriaDto(c.nombre, c.descripcion, c.icono);
    }

    private void copiar(CategoriaDto d, CategoriaEntity e) {
        e.nombre = d.getNombre();
        e.descripcion = d.getDescripcion();
        e.icono = d.getIcono();
        e.equiposVinculados = (int) d.getEquiposVinculados();
    }

    private String mensajeHttp(int codigo) {
        if (codigo == 401 || codigo == 403) {
            return "No tienes permiso para esta acción o tu sesión expiró.";
        }
        return "Error del servidor (" + codigo + ").";
    }

}