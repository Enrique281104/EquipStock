package edu.sv.ues.dam235.equipstock.views.equipment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.utils.EstadoEquipo;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;

public class EquiposFragment extends Fragment {

    private EquipoViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean esAdmin = new SessionManager(requireContext()).esAdmin();
        vm = new ViewModelProvider(this).get(EquipoViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvEquipos);
        TextView tvVacio = view.findViewById(R.id.tvVacio);
        EditText etBuscar = view.findViewById(R.id.etBuscar);
        ChipGroup chips = view.findViewById(R.id.chipGroup);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregar);

        EquipoAdapter adapter = new EquipoAdapter(
                e -> abrir(EquipoDetalleFragment.nueva(e.equipo.localId)));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        vm.equipos.observe(getViewLifecycleOwner(), lista -> {
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

        // Solo el administrador puede registrar equipos
        fab.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(v -> abrir(EquipoFormFragment.nuevo()));

        vm.sincronizarUnaVez((ok, mensaje) -> {
            if (!ok && getContext() != null) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String estadoDe(int chipId) {
        if (chipId == R.id.chipOperativo) return EstadoEquipo.OPERATIVO;
        if (chipId == R.id.chipMantenimiento) return EstadoEquipo.EN_MANTENIMIENTO;
        if (chipId == R.id.chipInactivo) return EstadoEquipo.INACTIVO;
        return "";
    }

    private int chipDe(String estado) {
        if (EstadoEquipo.OPERATIVO.equals(estado)) return R.id.chipOperativo;
        if (EstadoEquipo.EN_MANTENIMIENTO.equals(estado)) return R.id.chipMantenimiento;
        if (EstadoEquipo.INACTIVO.equals(estado)) return R.id.chipInactivo;
        return R.id.chipTodos;
    }

    private void abrir(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

}