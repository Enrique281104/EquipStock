package edu.sv.ues.dam235.equipstock.views.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import edu.sv.ues.dam235.equipstock.database.MantenimientoConDetalle;
import edu.sv.ues.dam235.equipstock.database.ResumenMantenimientos;
import edu.sv.ues.dam235.equipstock.repositories.EquipoRepository;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    public final LiveData<Integer> totalEquipos;
    public final LiveData<ResumenMantenimientos> resumen;
    public final LiveData<List<MantenimientoConDetalle>> lista;
    private final MutableLiveData<String> tab =
            new MutableLiveData<>(EstadoMantenimiento.VENCIDO);
    private boolean sincronizado = false;

    public DashboardViewModel(@NonNull Application app) {
        super(app);
        SessionManager session = new SessionManager(app);
        // El administrador ve todo (0); el técnico solo lo suyo
        final long tecnicoId = session.esAdmin() ? 0 : session.getUserId();

        MantenimientoRepository mantenimientos = MantenimientoRepository.get(app);
        totalEquipos = EquipoRepository.get(app).contar();
        resumen = mantenimientos.resumen(tecnicoId);
        lista = Transformations.switchMap(tab,
                estado -> mantenimientos.buscar("", estado, tecnicoId, 0));
    }

    public String getTab() {
        return tab.getValue();
    }

    public void setTab(String estado) {
        tab.setValue(estado);
    }

    public void sincronizarUnaVez(Resultado resultado) {
        if (sincronizado) return;
        sincronizado = true;
        SyncManager.get(getApplication()).sincronizar(resultado);
    }
}