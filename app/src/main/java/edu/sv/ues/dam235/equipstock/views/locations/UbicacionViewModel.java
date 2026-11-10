package edu.sv.ues.dam235.equipstock.views.locations;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import edu.sv.ues.dam235.equipstock.database.UbicacionEntity;
import edu.sv.ues.dam235.equipstock.repositories.UbicacionRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import java.util.List;

public class UbicacionViewModel extends AndroidViewModel {

    private final UbicacionRepository repo;
    private final MutableLiveData<String> filtro = new MutableLiveData<>("");
    public final LiveData<List<UbicacionEntity>> ubicaciones;
    private boolean sincronizado = false;

    public UbicacionViewModel(@NonNull Application app) {
        super(app);
        repo = UbicacionRepository.get(app);
        ubicaciones = Transformations.switchMap(filtro, texto -> repo.buscar(texto));
    }

    public void filtrar(String texto) {
        filtro.setValue(texto);
    }

    public void sincronizarUnaVez(Resultado resultado) {
        if (sincronizado) return;
        sincronizado = true;
        SyncManager.get(getApplication()).sincronizar(resultado);
    }
}