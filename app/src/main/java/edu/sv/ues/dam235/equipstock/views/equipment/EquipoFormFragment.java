package edu.sv.ues.dam235.equipstock.views.equipment;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.CategoriaEntity;
import edu.sv.ues.dam235.equipstock.database.EquipoEntity;
import edu.sv.ues.dam235.equipstock.database.UbicacionEntity;
import edu.sv.ues.dam235.equipstock.repositories.CategoriaRepository;
import edu.sv.ues.dam235.equipstock.repositories.EquipoRepository;
import edu.sv.ues.dam235.equipstock.repositories.UbicacionRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.utils.EstadoEquipo;
import java.util.ArrayList;
import java.util.List;

public class EquipoFormFragment extends Fragment {

    private static final String ARG_LOCAL_ID = "localId";
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_SERIE = "serie";
    private static final String ARG_CODIGO = "codigo";
    private static final String ARG_ESTADO = "estado";
    private static final String ARG_CATEGORIA = "categoria";
    private static final String ARG_UBICACION = "ubicacion";

    private List<CategoriaEntity> categorias = new ArrayList<>();
    private List<UbicacionEntity> ubicaciones = new ArrayList<>();
    private long categoriaSel = -1;
    private long ubicacionSel = -1;

    public static EquipoFormFragment nuevo() {
        return new EquipoFormFragment();
    }

    public static EquipoFormFragment editar(EquipoEntity e) {
        Bundle args = new Bundle();
        args.putLong(ARG_LOCAL_ID, e.localId);
        args.putString(ARG_NOMBRE, e.nombre);
        args.putString(ARG_SERIE, e.numeroSerie);
        args.putString(ARG_CODIGO, e.codigoActivo);
        args.putString(ARG_ESTADO, e.estado);
        args.putLong(ARG_CATEGORIA, e.categoriaLocalId == null ? -1 : e.categoriaLocalId);
        args.putLong(ARG_UBICACION, e.ubicacionLocalId == null ? -1 : e.ubicacionLocalId);
        EquipoFormFragment f = new EquipoFormFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipo_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        boolean edicion = args != null && args.containsKey(ARG_LOCAL_ID);
        long localId = edicion ? args.getLong(ARG_LOCAL_ID) : -1;
        if (edicion) {
            categoriaSel = args.getLong(ARG_CATEGORIA);
            ubicacionSel = args.getLong(ARG_UBICACION);
        }

        TextView tvTitulo = view.findViewById(R.id.tvTitulo);
        TextView tvSeccion = view.findViewById(R.id.tvSeccion);
        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        TextInputLayout tilNombre = view.findViewById(R.id.tilNombre);
        TextInputLayout tilSerie = view.findViewById(R.id.tilSerie);
        TextInputLayout tilCodigo = view.findViewById(R.id.tilCodigo);
        TextInputLayout tilCategoria = view.findViewById(R.id.tilCategoria);
        TextInputLayout tilUbicacion = view.findViewById(R.id.tilUbicacion);
        TextInputEditText etNombre = view.findViewById(R.id.etNombre);
        TextInputEditText etSerie = view.findViewById(R.id.etSerie);
        TextInputEditText etCodigo = view.findViewById(R.id.etCodigo);
        MaterialAutoCompleteTextView acCategoria = view.findViewById(R.id.acCategoria);
        MaterialAutoCompleteTextView acUbicacion = view.findViewById(R.id.acUbicacion);
        ChipGroup chips = view.findViewById(R.id.chipsEstado);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardar);

        tvTitulo.setText(edicion ? R.string.equipo_form_editar : R.string.equipo_form_nuevo);
        tvSeccion.setText(edicion ? R.string.equipo_form_info : R.string.equipo_form_datos);
        btnGuardar.setText(edicion
                ? R.string.equipo_form_actualizar : R.string.equipo_form_guardar);

        if (edicion && savedInstanceState == null) {
            etNombre.setText(args.getString(ARG_NOMBRE));
            etSerie.setText(args.getString(ARG_SERIE));
            etCodigo.setText(args.getString(ARG_CODIGO));
        }
        chips.check(chipDe(edicion ? args.getString(ARG_ESTADO) : EstadoEquipo.OPERATIVO));

        // Las listas desplegables salen de las tablas locales
        CategoriaRepository.get(requireContext()).todas()
                .observe(getViewLifecycleOwner(), lista -> {
                    categorias = lista;
                    String[] nombres = new String[lista.size()];
                    String elegido = null;
                    for (int i = 0; i < lista.size(); i++) {
                        nombres[i] = lista.get(i).nombre;
                        if (lista.get(i).localId == categoriaSel) elegido = nombres[i];
                    }
                    acCategoria.setSimpleItems(nombres);
                    if (elegido != null) acCategoria.setText(elegido, false);
                });
        acCategoria.setOnItemClickListener((parent, v, pos, id) -> {
            categoriaSel = categorias.get(pos).localId;
            tilCategoria.setError(null);
        });

        UbicacionRepository.get(requireContext()).todas()
                .observe(getViewLifecycleOwner(), lista -> {
                    ubicaciones = lista;
                    String[] nombres = new String[lista.size()];
                    String elegido = null;
                    for (int i = 0; i < lista.size(); i++) {
                        nombres[i] = lista.get(i).nombre;
                        if (lista.get(i).localId == ubicacionSel) elegido = nombres[i];
                    }
                    acUbicacion.setSimpleItems(nombres);
                    if (elegido != null) acUbicacion.setText(elegido, false);
                });
        acUbicacion.setOnItemClickListener((parent, v, pos, id) -> {
            ubicacionSel = ubicaciones.get(pos).localId;
            tilUbicacion.setError(null);
        });

        // El fragmento se cierra antes de que termine el envío, así que el aviso
        // se muestra con el contexto de la aplicación.
        Context app = requireContext().getApplicationContext();
        Resultado aviso = (ok, mensaje) -> {
            if (!ok) Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
        };

        EquipoRepository repo = EquipoRepository.get(requireContext());

        btnVolver.setOnClickListener(v -> cerrar(v));

        btnGuardar.setOnClickListener(v -> {
            tilNombre.setError(null);
            tilSerie.setError(null);
            tilCodigo.setError(null);
            tilCategoria.setError(null);
            tilUbicacion.setError(null);

            String nombre = texto(etNombre).trim();
            String serie = texto(etSerie).trim();
            String codigo = texto(etCodigo).trim();

            boolean valido = true;
            if (nombre.length() < 3) {
                tilNombre.setError("Ingresa el nombre del equipo");
                valido = false;
            }
            if (serie.isEmpty()) {
                tilSerie.setError("Ingresa el número de serie");
                valido = false;
            }
            if (codigo.isEmpty()) {
                tilCodigo.setError("Ingresa el código de activo");
                valido = false;
            }
            if (categoriaSel < 0) {
                tilCategoria.setError("Selecciona una categoría");
                valido = false;
            }
            if (ubicacionSel < 0) {
                tilUbicacion.setError("Selecciona una ubicación");
                valido = false;
            }
            if (!valido) return;

            String estado = estadoActual(chips);
            btnGuardar.setEnabled(false);
            repo.validarDuplicados(edicion ? localId : -1, serie, codigo,
                    (codigoRepetido, serieRepetida) -> {
                        if (!isAdded() || getView() == null) return;
                        btnGuardar.setEnabled(true);
                        if (codigoRepetido) {
                            tilCodigo.setError("Ya existe un equipo con este código");
                        }
                        if (serieRepetida) {
                            tilSerie.setError("Ya existe un equipo con este número de serie");
                        }
                        if (codigoRepetido || serieRepetida) return;

                        if (edicion) {
                            repo.editar(localId, nombre, serie, codigo, estado,
                                    categoriaSel, ubicacionSel, aviso);
                        } else {
                            repo.crear(nombre, serie, codigo, estado,
                                    categoriaSel, ubicacionSel, aviso);
                        }
                        cerrar(v);
                    });
        });
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }

    private String estadoActual(ChipGroup chips) {
        int id = chips.getCheckedChipId();
        if (id == R.id.chipEstadoMantenimiento) return EstadoEquipo.EN_MANTENIMIENTO;
        if (id == R.id.chipEstadoInactivo) return EstadoEquipo.INACTIVO;
        return EstadoEquipo.OPERATIVO;
    }

    private int chipDe(String estado) {
        if (EstadoEquipo.EN_MANTENIMIENTO.equals(estado)) return R.id.chipEstadoMantenimiento;
        if (EstadoEquipo.INACTIVO.equals(estado)) return R.id.chipEstadoInactivo;
        return R.id.chipEstadoOperativo;
    }

    private void cerrar(View v) {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        getParentFragmentManager().popBackStack();
    }
}