package edu.sv.ues.dam235.equipstock.views.maintenance;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import edu.sv.ues.dam235.equipstock.database.MantenimientoConDetalle;
import edu.sv.ues.dam235.equipstock.database.TecnicoEntity;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.repositories.TecnicoRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.util.List;

public class MantenimientoViewModel extends AndroidViewModel {

    private static class Filtro {
        final String texto;
        final String estado;
        final long tecnicoId;
        final long equipoLocalId;

        Filtro(String texto, String estado, long tecnicoId, long equipoLocalId) {
            this.texto = texto;
            this.estado = estado;
            this.tecnicoId = tecnicoId;
            this.equipoLocalId = equipoLocalId;
        }
    }

    private final MutableLiveData<Filtro> filtro;
    public final LiveData<List<MantenimientoConDetalle>> mantenimientos;
    public final LiveData<List<TecnicoEntity>> tecnicos;
    private final boolean esAdmin;
    private final long userId;
    private String texto = "";
    private String estado = "";
    private long tecnicoId;
    private long equipoLocalId = 0;
    private boolean equipoInicialAplicado = false;
    private boolean sincronizado = false;

    public MantenimientoViewModel(@NonNull Application app) {
        super(app);
        SessionManager session = new SessionManager(app);
        esAdmin = session.esAdmin();
        userId = session.getUserId();
        // El técnico solo ve los suyos; el admin ve todos (0 = sin filtro)
        tecnicoId = esAdmin ? 0 : userId;

        filtro = new MutableLiveData<>(new Filtro(texto, estado, tecnicoId, equipoLocalId));
        MantenimientoRepository repo = MantenimientoRepository.get(app);
        mantenimientos = Transformations.switchMap(filtro,
                f -> repo.buscar(f.texto, f.estado, f.tecnicoId, f.equipoLocalId));
        tecnicos = TecnicoRepository.get(app).todos();
    }

    public String getTexto() { return texto; }

    public String getEstado() { return estado; }

    public long getTecnicoId() { return tecnicoId; }

    public long getEquipoLocalId() { return equipoLocalId; }

    public void filtrarTexto(String nuevo) {
        texto = nuevo;
        aplicar();
    }

    public void filtrarEstado(String nuevo) {
        estado = nuevo;
        aplicar();
    }

    public void filtrarTecnico(long nuevo) {
        tecnicoId = nuevo;
        aplicar();
    }

    /** Filtro por equipo. Al ver el historial de un equipo, el técnico ve todos los registros. */
    public void filtrarEquipo(long nuevo) {
        equipoLocalId = nuevo;
        if (!esAdmin) tecnicoId = nuevo == 0 ? userId : 0;
        aplicar();
    }

    /** Aplica el equipo con el que se abrió la pantalla, una sola vez. */
    public void aplicarEquipoInicial(long id) {
        if (equipoInicialAplicado) return;
        equipoInicialAplicado = true;
        filtrarEquipo(id);
    }

    private void aplicar() {
        filtro.setValue(new Filtro(texto, estado, tecnicoId, equipoLocalId));
    }

    public void sincronizarUnaVez(Resultado resultado) {
        if (sincronizado) return;
        sincronizado = true;
        SyncManager.get(getApplication()).sincronizar(resultado);
    }
}