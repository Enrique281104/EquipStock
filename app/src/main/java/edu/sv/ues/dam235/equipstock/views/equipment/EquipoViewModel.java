package edu.sv.ues.dam235.equipstock.views.equipment;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import edu.sv.ues.dam235.equipstock.database.EquipoConDetalle;
import edu.sv.ues.dam235.equipstock.repositories.EquipoRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import java.util.List;

public class EquipoViewModel extends AndroidViewModel {

    private static class Filtro {
        final String texto;
        final String estado;

        Filtro(String texto, String estado) {
            this.texto = texto;
            this.estado = estado;
        }
    }

    private final MutableLiveData<Filtro> filtro = new MutableLiveData<>(new Filtro("", ""));
    public final LiveData<List<EquipoConDetalle>> equipos;
    private String texto = "";
    private String estado = "";
    private boolean sincronizado = false;

    public EquipoViewModel(@NonNull Application app) {
        super(app);
        EquipoRepository repo = EquipoRepository.get(app);
        equipos = Transformations.switchMap(filtro, f -> repo.buscar(f.texto, f.estado));
    }

    public String getTexto() { return texto; }

    public String getEstado() { return estado; }

    public void filtrarTexto(String nuevo) {
        texto = nuevo;
        filtro.setValue(new Filtro(texto, estado));
    }

    public void filtrarEstado(String nuevo) {
        estado = nuevo;
        filtro.setValue(new Filtro(texto, estado));
    }

    public void sincronizarUnaVez(Resultado resultado) {
        if (sincronizado) return;
        sincronizado = true;
        SyncManager.get(getApplication()).sincronizar(resultado);
    }
}