package edu.sv.ues.dam235.equipstock.views.categories;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;

public class CategoriasFragment extends Fragment {

    private CategoriaViewModel vm;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean esAdmin = new SessionManager(requireContext()).esAdmin();
        vm = new ViewModelProvider(this).get(CategoriaViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvCategorias);
        TextView tvVacio = view.findViewById(R.id.tvVacio);
        EditText etBuscar = view.findViewById(R.id.etBuscar);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregar);

        CategoriaAdapter adapter = new CategoriaAdapter(
                c -> abrirFormulario(CategoriaFormFragment.editar(c)), esAdmin);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        vm.categorias.observe(getViewLifecycleOwner(), lista -> {
            adapter.submitList(lista);
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                vm.filtrar(s.toString().trim());
            }
        });

        // Solo el administrador puede agregar categorías
        fab.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(v -> abrirFormulario(CategoriaFormFragment.nueva()));

        // Al abrir la pantalla se sincroniza con el servidor
        vm.sincronizarUnaVez((ok, mensaje) -> {
            if (!ok && getContext() != null) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirFormulario(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

}