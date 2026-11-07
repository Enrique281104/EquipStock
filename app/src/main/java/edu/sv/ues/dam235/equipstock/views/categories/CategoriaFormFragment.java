package edu.sv.ues.dam235.equipstock.views.categories;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.CategoriaEntity;
import edu.sv.ues.dam235.equipstock.repositories.CategoriaRepository;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;

public class CategoriaFormFragment extends Fragment {

    private static final String ARG_LOCAL_ID = "localId";
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_DESCRIPCION = "descripcion";
    private static final String ARG_ICONO = "icono";
    private static final String ARG_VINCULADOS = "vinculados";

    public static CategoriaFormFragment nueva() {
        return new CategoriaFormFragment();
    }

    public static CategoriaFormFragment editar(CategoriaEntity c) {
        Bundle args = new Bundle();
        args.putLong(ARG_LOCAL_ID, c.localId);
        args.putString(ARG_NOMBRE, c.nombre);
        args.putString(ARG_DESCRIPCION, c.descripcion);
        args.putString(ARG_ICONO, c.icono);
        args.putInt(ARG_VINCULADOS, c.equiposVinculados);
        CategoriaFormFragment f = new CategoriaFormFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categoria_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        boolean edicion = args != null && args.containsKey(ARG_LOCAL_ID);
        long localId = edicion ? args.getLong(ARG_LOCAL_ID) : -1;
        String iconoOriginal = edicion ? args.getString(ARG_ICONO) : null;
        String nombreOriginal = edicion ? args.getString(ARG_NOMBRE) : null;
        int vinculados = edicion ? args.getInt(ARG_VINCULADOS) : 0;

        TextView tvTitulo = view.findViewById(R.id.tvTitulo);
        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        ImageButton btnEliminar = view.findViewById(R.id.btnEliminar);
        TextInputLayout tilNombre = view.findViewById(R.id.tilNombre);
        TextInputEditText etNombre = view.findViewById(R.id.etNombre);
        TextInputEditText etDescripcion = view.findViewById(R.id.etDescripcion);
        RecyclerView rvIconos = view.findViewById(R.id.rvIconos);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardar);

        tvTitulo.setText(edicion
                ? R.string.categoria_form_editar : R.string.categoria_form_registrar);
        btnEliminar.setVisibility(edicion ? View.VISIBLE : View.GONE);

        if (edicion && savedInstanceState == null) {
            etNombre.setText(nombreOriginal);
            etDescripcion.setText(args.getString(ARG_DESCRIPCION));
        }

        IconoAdapter iconos = new IconoAdapter(IconosCategoria.NOMBRES, iconoOriginal);
        rvIconos.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rvIconos.setAdapter(iconos);

        // El fragmento se cierra antes de que termine el envío, así que el aviso
        // se muestra con el contexto de la aplicación.
        Context app = requireContext().getApplicationContext();
        CategoriaRepository.Resultado aviso = (ok, mensaje) -> {
            if (!ok) Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
        };

        btnVolver.setOnClickListener(v -> cerrar(v));

        btnGuardar.setOnClickListener(v -> {
            String nombre = texto(etNombre).trim();
            String descripcion = texto(etDescripcion).trim();

            tilNombre.setError(null);
            if (nombre.length() < 3) {
                tilNombre.setError("Ingresa el nombre de la categoría");
                return;
            }
            String icono = iconos.getSeleccionado() != null
                    ? iconos.getSeleccionado() : iconoOriginal;
            if (icono == null) {
                Toast.makeText(requireContext(), "Selecciona un ícono",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            CategoriaRepository repo = CategoriaRepository.get(requireContext());
            if (edicion) {
                repo.editar(localId, nombre, descripcion, icono, aviso);
            } else {
                repo.crear(nombre, descripcion, icono, aviso);
            }
            cerrar(v);
        });

        btnEliminar.setOnClickListener(v -> {
            if (vinculados > 0) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("No se puede eliminar")
                        .setMessage("«" + nombreOriginal + "» tiene " + vinculados
                                + " equipo(s) vinculado(s). Reasígnalos antes de eliminarla.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("¿Eliminar categoría?")
                    .setMessage("Se eliminará «" + nombreOriginal
                            + "». Esta acción no se puede deshacer.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar", (d, w) -> {
                        CategoriaRepository.get(requireContext()).eliminar(localId, aviso);
                        cerrar(v);
                    })
                    .show();
        });
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }

    private void cerrar(View v) {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        getParentFragmentManager().popBackStack();
    }
}