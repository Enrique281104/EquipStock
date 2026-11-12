package edu.sv.ues.dam235.equipstock.repositories;

import android.content.Context;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.PerfilApi;
import edu.sv.ues.dam235.equipstock.models.Usuario;
import edu.sv.ues.dam235.equipstock.sync.Sincronizable;
import edu.sv.ues.dam235.equipstock.utils.FotoUtil;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class PerfilRepository implements Sincronizable {

    private static PerfilRepository instance;

    public static synchronized PerfilRepository get(Context context) {
        if (instance == null) {
            instance = new PerfilRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final Context appContext;
    private final PerfilApi api;
    private final SessionManager session;

    private PerfilRepository(Context appContext) {
        this.appContext = appContext;
        api = ApiClient.get(appContext).create(PerfilApi.class);
        session = new SessionManager(appContext);
    }

    @Override
    public String sincronizarAhora() throws IOException {
        Response<Usuario> r = api.obtener().execute();
        if (!r.isSuccessful() || r.body() == null) {
            if (r.code() == 401 || r.code() == 403) {
                return "No tienes permiso para esta acción o tu sesión expiró.";
            }
            return "Error del servidor (" + r.code() + ").";
        }

        Usuario u = r.body();
        session.actualizarPerfil(u.getNombre(), u.getTelefono());
        long version = u.getFotoActualizada() == null ? 0 : u.getFotoActualizada();
        session.setFotoActualizada(version);

        if (version == 0) {
            FotoUtil.borrar(appContext);
        } else if (FotoUtil.versionLocal(appContext) != version) {
            Response<ResponseBody> d = api.descargarFoto().execute();
            if (d.isSuccessful() && d.body() != null) {
                FotoUtil.guardar(appContext, d.body().bytes(), version);
            }
        }
        return null;
    }
}