package edu.sv.ues.dam235.equipstock.views.maintenance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.TecnicoEntity;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class MantenimientosFragment extends Fragment {

    private static final String ARG_EQUIPO = "equipo";
    private static final String ARG_EQUIPO_NOMBRE = "equipoNombre";

    /** Abre la lista filtrada por un equipo (historial completo). */
    public static MantenimientosFragment deEquipo(long equipoLocalId, String nombre) {
        Bundle args = new Bundle();
        args.putLong(ARG_EQUIPO, equipoLocalId);
        args.putString(ARG_EQUIPO_NOMBRE, nombre);
        MantenimientosFragment f = new MantenimientosFragment();
        f.setArguments(args);
        return f;
    }

    private MantenimientoViewModel vm;
    private List<TecnicoEntity> tecnicos = new ArrayList<>();
    private boolean esAdmin;
    private String nombreEquipo;
    private View filaFiltros;
    private TextView tvFiltrosActivos;
    private Chip chipTecnico;
    private Chip chipEquipo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mantenimientos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        esAdmin = new SessionManager(requireContext()).esAdmin();
        vm = new ViewModelProvider(this).get(MantenimientoViewModel.class);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_EQUIPO)) {
            nombreEquipo = args.getString(ARG_EQUIPO_NOMBRE);
            vm.aplicarEquipoInicial(args.getLong(ARG_EQUIPO));
        }

        RecyclerView rv = view.findViewById(R.id.rvMantenimientos);
        TextView tvVacio = view.findViewById(R.id.tvVacio);
        EditText etBuscar = view.findViewById(R.id.etBuscar);
        ChipGroup chips = view.findViewById(R.id.chipGroup);
        ImageButton btnFiltro = view.findViewById(R.id.btnFiltroTecnico);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregar);
        filaFiltros = view.findViewById(R.id.filaFiltros);
        tvFiltrosActivos = view.findViewById(R.id.tvFiltrosActivos);
        chipTecnico = view.findViewById(R.id.chipTecnico);
        chipEquipo = view.findViewById(R.id.chipEquipo);

        MantenimientoAdapter adapter = new MantenimientoAdapter(
                m -> abrir(MantenimientoDetalleFragment.nueva(m.mantenimiento.localId)));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        vm.mantenimientos.observe(getViewLifecycleOwner(), lista -> {
            adapter.submitList(lista);
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Restaura los filtros si la pantalla se vuelve a crear
        etBuscar.setText(vm.getTexto());
        chips.check(chipDe(vm.getEstado()));

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                vm.filtrarTexto(s.toString().trim());
            }
        });

        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) vm.filtrarEstado(estadoDe(ids.get(0)));
        });

        // El filtro por técnico es solo del administrador
        btnFiltro.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
        if (esAdmin) {
            vm.tecnicos.observe(getViewLifecycleOwner(), lista -> {
                tecnicos = lista;
                actualizarFiltros();
            });
            btnFiltro.setOnClickListener(v -> elegirTecnico());
            chipTecnico.setOnCloseIconClickListener(v -> {
                vm.filtrarTecnico(0);
                actualizarFiltros();
            });
        }
        chipEquipo.setOnCloseIconClickListener(v -> {
            vm.filtrarEquipo(0);
            actualizarFiltros();
        });
        actualizarFiltros();

        // Admin y técnico pueden registrar (el técnico, solo asignados a sí mismo)
        fab.setOnClickListener(v -> abrir(MantenimientoFormFragment.nuevo()));

        vm.sincronizarUnaVez((ok, mensaje) -> {
            if (!ok && getContext() != null) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void elegirTecnico() {
        String[] opciones = new String[tecnicos.size() + 1];
        opciones[0] = "Todos los técnicos";
        for (int i = 0; i < tecnicos.size(); i++) {
            opciones[i + 1] = tecnicos.get(i).nombre;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.mantenimientos_filtrar_tecnico)
                .setItems(opciones, (dialog, which) -> {
                    vm.filtrarTecnico(which == 0 ? 0 : tecnicos.get(which - 1).id);
                    actualizarFiltros();
                })
                .show();
    }

    private void actualizarFiltros() {
        boolean hayTecnico = esAdmin && vm.getTecnicoId() != 0;
        boolean hayEquipo = vm.getEquipoLocalId() != 0;

        filaFiltros.setVisibility(esAdmin || hayEquipo ? View.VISIBLE : View.GONE);
        tvFiltrosActivos.setVisibility(hayTecnico || hayEquipo ? View.VISIBLE : View.GONE);

        if (hayTecnico) {
            String nombre = "Técnico";
            for (TecnicoEntity t : tecnicos) {
                if (t.id == vm.getTecnicoId()) nombre = t.nombre;
            }
            chipTecnico.setText("Técnico: " + nombre);
            chipTecnico.setVisibility(View.VISIBLE);
        } else {
            chipTecnico.setVisibility(View.GONE);
        }

        if (hayEquipo) {
            chipEquipo.setText("Equipo: " + (nombreEquipo == null ? "-" : nombreEquipo));
            chipEquipo.setVisibility(View.VISIBLE);
        } else {
            chipEquipo.setVisibility(View.GONE);
        }
    }

    private String estadoDe(int chipId) {
        if (chipId == R.id.chipCompletado) return EstadoMantenimiento.COMPLETADO;
        if (chipId == R.id.chipProceso) return EstadoMantenimiento.EN_PROCESO;
        if (chipId == R.id.chipVencido) return EstadoMantenimiento.VENCIDO;
        return "";
    }

    private int chipDe(String estado) {
        if (EstadoMantenimiento.COMPLETADO.equals(estado)) return R.id.chipCompletado;
        if (EstadoMantenimiento.EN_PROCESO.equals(estado)) return R.id.chipProceso;
        if (EstadoMantenimiento.VENCIDO.equals(estado)) return R.id.chipVencido;
        return R.id.chipTodos;
    }

    private void abrir(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}