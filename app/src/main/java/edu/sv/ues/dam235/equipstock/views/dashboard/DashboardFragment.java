package edu.sv.ues.dam235.equipstock.views.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.utils.ConexionLiveData;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import edu.sv.ues.dam235.equipstock.views.maintenance.MantenimientoAdapter;
import edu.sv.ues.dam235.equipstock.views.maintenance.MantenimientoDetalleFragment;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private static final int MAX_ITEMS = 15;

    private boolean esAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = requireContext();
        esAdmin = new SessionManager(ctx).esAdmin();
        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        TextView tvHola = view.findViewById(R.id.tvHola);
        TextView tvLabel1 = view.findViewById(R.id.tvLabel1);
        TextView tvLabel2 = view.findViewById(R.id.tvLabel2);
        ImageView ivIcon1 = view.findViewById(R.id.ivIcon1);
        TextView tvNumero1 = view.findViewById(R.id.tvNumero1);
        TextView tvNumero2 = view.findViewById(R.id.tvNumero2);
        TextView tvConexion = view.findViewById(R.id.tvConexion);
        DonutView donut = view.findViewById(R.id.donut);
        TextView tvLeyCompletados = view.findViewById(R.id.tvLeyCompletados);
        TextView tvLeyProceso = view.findViewById(R.id.tvLeyProceso);
        TextView tvLeyVencidos = view.findViewById(R.id.tvLeyVencidos);
        ChipGroup chips = view.findViewById(R.id.chipsTab);
        Chip chipPrimero = view.findViewById(R.id.chipTabPrimero);
        TextView tvVacio = view.findViewById(R.id.tvVacio);
        RecyclerView rv = view.findViewById(R.id.rvLista);

        // Textos según el rol
        tvHola.setText(esAdmin ? R.string.dashboard_hola_admin : R.string.dashboard_hola_tecnico);
        tvLabel1.setText(esAdmin ? R.string.dashboard_total_equipos : R.string.dashboard_pendientes);
        tvLabel2.setText(esAdmin
                ? R.string.dashboard_vencidos : R.string.dashboard_vencidos_acumulados);
        ivIcon1.setImageResource(esAdmin ? R.drawable.ic_north_east : R.drawable.ic_schedule);
        chipPrimero.setText(esAdmin
                ? R.string.dashboard_tab_completados : R.string.dashboard_tab_todos);

        // Encabezado y gráfica
        if (esAdmin) {
            vm.totalEquipos.observe(getViewLifecycleOwner(),
                    n -> tvNumero1.setText(String.valueOf(n == null ? 0 : n)));
        }
        vm.resumen.observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            donut.setValores(r.completados, r.enProceso, r.vencidos);
            tvLeyCompletados.setText(String.valueOf(r.completados));
            tvLeyProceso.setText(String.valueOf(r.enProceso));
            tvLeyVencidos.setText(String.valueOf(r.vencidos));
            tvNumero2.setText(String.valueOf(r.vencidos));
            if (!esAdmin) tvNumero1.setText(String.valueOf(r.enProceso));
        });

        // Indicador de conexión
        new ConexionLiveData(ctx).observe(getViewLifecycleOwner(), enLinea -> {
            boolean ok = enLinea != null && enLinea;
            tvConexion.setText(ok ? R.string.dashboard_en_linea : R.string.dashboard_sin_conexion);
            tvConexion.setTextColor(ContextCompat.getColor(ctx,
                    ok ? R.color.on_primary : R.color.estado_mantenimiento_text));
            tvConexion.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx,
                    ok ? R.color.primary : R.color.estado_mantenimiento_bg)));
        });

        // Pestañas y lista
        chips.check(chipDe(vm.getTab()));
        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) vm.setTab(estadoDe(ids.get(0)));
        });

        MantenimientoAdapter adapter = new MantenimientoAdapter(m ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer,
                                MantenimientoDetalleFragment.nueva(m.mantenimiento.localId))
                        .addToBackStack(null)
                        .commit());
        rv.setLayoutManager(new LinearLayoutManager(ctx));
        rv.setAdapter(adapter);

        vm.lista.observe(getViewLifecycleOwner(), lista -> {
            int n = Math.min(lista.size(), MAX_ITEMS);
            adapter.submitList(new ArrayList<>(lista.subList(0, n)));
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        vm.sincronizarUnaVez((ok, mensaje) -> {
            if (!ok && getContext() != null) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String estadoDe(int chipId) {
        if (chipId == R.id.chipTabProceso) return EstadoMantenimiento.EN_PROCESO;
        if (chipId == R.id.chipTabVencido) return EstadoMantenimiento.VENCIDO;
        return esAdmin ? EstadoMantenimiento.COMPLETADO : "";
    }

    private int chipDe(String estado) {
        if (EstadoMantenimiento.EN_PROCESO.equals(estado)) return R.id.chipTabProceso;
        if (EstadoMantenimiento.VENCIDO.equals(estado)) return R.id.chipTabVencido;
        return R.id.chipTabPrimero;
    }
}