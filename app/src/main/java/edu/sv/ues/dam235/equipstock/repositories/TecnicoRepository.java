package edu.sv.ues.dam235.equipstock.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.TecnicoApi;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.database.TecnicoDao;
import edu.sv.ues.dam235.equipstock.database.TecnicoEntity;
import edu.sv.ues.dam235.equipstock.models.TecnicoDto;
import edu.sv.ues.dam235.equipstock.sync.Sincronizable;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class TecnicoRepository implements Sincronizable {

    private static TecnicoRepository instance;

    public static synchronized TecnicoRepository get(Context context) {
        if (instance == null) {
            instance = new TecnicoRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final AppDatabase db;
    private final TecnicoDao dao;
    private final TecnicoApi api;
    private final SessionManager session;

    private TecnicoRepository(Context appContext) {
        db = AppDatabase.get(appContext);
        dao = db.tecnicoDao();
        api = ApiClient.get(appContext).create(TecnicoApi.class);
        session = new SessionManager(appContext);
    }

    public LiveData<List<TecnicoEntity>> todos() {
        return dao.todos();
    }

    @Override
    public String sincronizarAhora() throws IOException {
        if (!session.esAdmin()) return null;   // solo el administrador necesita la lista

        Response<List<TecnicoDto>> r = api.listar().execute();
        if (!r.isSuccessful() || r.body() == null) {
            return "Error del servidor (" + r.code() + ").";
        }

        List<TecnicoEntity> lista = new ArrayList<>();
        for (TecnicoDto d : r.body()) {
            TecnicoEntity t = new TecnicoEntity();
            t.id = d.getId();
            t.nombre = d.getNombre();
            lista.add(t);
        }
        db.runInTransaction(() -> {
            dao.borrarTodos();
            dao.insertar(lista);
        });
        return null;
    }
}