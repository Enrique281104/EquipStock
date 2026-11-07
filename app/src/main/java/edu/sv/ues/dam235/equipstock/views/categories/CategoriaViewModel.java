package edu.sv.ues.dam235.equipstock.views.categories;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import edu.sv.ues.dam235.equipstock.database.CategoriaEntity;
import edu.sv.ues.dam235.equipstock.repositories.CategoriaRepository;
import java.util.List;

public class CategoriaViewModel extends AndroidViewModel {

    private final CategoriaRepository repo;
    private final MutableLiveData<String> filtro = new MutableLiveData<>("");
    public final LiveData<List<CategoriaEntity>> categorias;

    private boolean sincronizado = false;
    public CategoriaViewModel(@NonNull Application app) {
        super(app);
        repo = CategoriaRepository.get(app);
        categorias = Transformations.switchMap(filtro, texto -> repo.buscar(texto));
    }

    public void filtrar(String texto) {
        filtro.setValue(texto);
    }

    public void sincronizarUnaVez(CategoriaRepository.Resultado resultado) {
        if (sincronizado) return;
        sincronizado = true;
        repo.sincronizar(resultado);
    }
}