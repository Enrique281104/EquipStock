package edu.sv.ues.dam235.equipstock.views.maintenance;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.EquipoConDetalle;
import edu.sv.ues.dam235.equipstock.database.MantenimientoEntity;
import edu.sv.ues.dam235.equipstock.database.TecnicoEntity;
import edu.sv.ues.dam235.equipstock.repositories.EquipoRepository;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.repositories.TecnicoRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.FechaUtil;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MantenimientoFormFragment extends Fragment {

    private static final String ARG_LOCAL_ID = "localId";
    private static final String ARG_EQUIPO = "equipo";
    private static final String ARG_TECNICO_ID = "tecnicoId";
    private static final String ARG_TECNICO_NOMBRE = "tecnicoNombre";
    private static final String ARG_TIPO = "tipo";
    private static final String ARG_ESTADO = "estado";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_DESCRIPCION = "descripcion";
    private static final String ARG_OBSERVACIONES = "observaciones";

    private EquipoConDetalle equipoSel;
    private String fechaSel;
    private long tecnicoSelId = -1;
    private String tecnicoSelNombre;
    private List<TecnicoEntity> tecnicos = new ArrayList<>();

    public static MantenimientoFormFragment nuevo() {
        return new MantenimientoFormFragment();
    }

    public static MantenimientoFormFragment editar(MantenimientoEntity m) {
        Bundle args = new Bundle();
        args.putLong(ARG_LOCAL_ID, m.localId);
        args.putLong(ARG_EQUIPO, m.equipoLocalId == null ? -1 : m.equipoLocalId);
        args.putLong(ARG_TECNICO_ID, m.tecnicoId == null ? -1 : m.tecnicoId);
        args.putString(ARG_TECNICO_NOMBRE, m.tecnicoNombre);
        args.putString(ARG_TIPO, m.tipo);
        args.putString(ARG_ESTADO, m.estado);
        args.putString(ARG_FECHA, m.fechaProgramada);
        args.putString(ARG_DESCRIPCION, m.descripcion);
        args.putString(ARG_OBSERVACIONES, m.observaciones);
        MantenimientoFormFragment f = new MantenimientoFormFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mantenimiento_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = requireContext();
        SessionManager session = new SessionManager(ctx);
        boolean esAdmin = session.esAdmin();
        Bundle args = getArguments();
        boolean edicion = args != null && args.containsKey(ARG_LOCAL_ID);
        long localId = edicion ? args.getLong(ARG_LOCAL_ID) : -1;

        TextView tvTitulo = view.findViewById(R.id.tvTitulo);
        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        TextInputLayout tilActivo = view.findViewById(R.id.tilActivo);
        TextInputEditText etActivo = view.findViewById(R.id.etActivo);
        MaterialCardView cardEquipo = view.findViewById(R.id.cardEquipo);
        ImageView ivIcono = view.findViewById(R.id.ivIcono);
        TextView tvEquipo = view.findViewById(R.id.tvEquipo);
        TextView tvCodigo = view.findViewById(R.id.tvCodigo);
        TextView tvSerie = view.findViewById(R.id.tvSerie);
        TextView tvUbicacion = view.findViewById(R.id.tvUbicacion);
        ChipGroup chipsTipo = view.findViewById(R.id.chipsTipo);
        TextInputLayout tilTecnico = view.findViewById(R.id.tilTecnico);
        MaterialAutoCompleteTextView acTecnico = view.findViewById(R.id.acTecnico);
        TextInputLayout tilFecha = view.findViewById(R.id.tilFecha);
        TextInputEditText etFecha = view.findViewById(R.id.etFecha);
        ChipGroup chipsEstado = view.findViewById(R.id.chipsEstadoTarea);
        TextInputLayout tilDescripcion = view.findViewById(R.id.tilDescripcion);
        TextInputEditText etDescripcion = view.findViewById(R.id.etDescripcion);
        TextInputEditText etObservaciones = view.findViewById(R.id.etObservaciones);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardar);

        tvTitulo.setText(edicion
                ? R.string.mantenimiento_form_editar : R.string.mantenimiento_form_nuevo);
        btnGuardar.setText(edicion
                ? R.string.mantenimiento_form_actualizar : R.string.mantenimiento_form_guardar);

        // ----- Valores iniciales -----
        if (edicion && savedInstanceState == null) {
            etDescripcion.setText(args.getString(ARG_DESCRIPCION));
            etObservaciones.setText(args.getString(ARG_OBSERVACIONES));
        }
        chipsTipo.check(chipTipoDe(edicion ? args.getString(ARG_TIPO) : "PREVENTIVO"));
        chipsEstado.check(edicion
                && EstadoMantenimiento.COMPLETADO.equals(args.getString(ARG_ESTADO))
                ? R.id.chipTareaCompletado : R.id.chipTareaProceso);

        fechaSel = edicion ? args.getString(ARG_FECHA) : FechaUtil.hoy();
        if (fechaSel == null) fechaSel = FechaUtil.hoy();
        etFecha.setText(FechaUtil.mostrar(fechaSel));
        etFecha.setOnClickListener(v -> elegirFecha(etFecha, tilFecha));
        tilFecha.setEndIconOnClickListener(v -> elegirFecha(etFecha, tilFecha));

        // ----- Técnico: el admin elige; el técnico queda fijo en sí mismo -----
        if (edicion) {
            tecnicoSelId = args.getLong(ARG_TECNICO_ID);
            tecnicoSelNombre = args.getString(ARG_TECNICO_NOMBRE);
        } else if (!esAdmin) {
            tecnicoSelId = session.getUserId();
            tecnicoSelNombre = session.getNombre();
        }
        if (esAdmin) {
            TecnicoRepository.get(ctx).todos().observe(getViewLifecycleOwner(), lista -> {
                tecnicos = lista;
                String[] nombres = new String[lista.size()];
                for (int i = 0; i < lista.size(); i++) nombres[i] = lista.get(i).nombre;
                acTecnico.setSimpleItems(nombres);
                if (tecnicoSelId >= 0 && tecnicoSelNombre != null) {
                    acTecnico.setText(tecnicoSelNombre, false);
                }
            });
            acTecnico.setOnItemClickListener((parent, v, pos, id) -> {
                tecnicoSelId = tecnicos.get(pos).id;
                tecnicoSelNombre = tecnicos.get(pos).nombre;
                tilTecnico.setError(null);
            });
        } else {
            acTecnico.setText(tecnicoSelNombre, false);
            tilTecnico.setEnabled(false);
        }

        // ----- Activo: en edición queda fijo; en alta se busca por código o serie -----
        Consumer<EquipoConDetalle> mostrarEquipo = d -> {
            equipoSel = d;
            cardEquipo.setVisibility(d == null ? View.GONE : View.VISIBLE);
            if (d == null) return;
            tilActivo.setError(null);
            ivIcono.setImageResource(IconosCategoria.recurso(ctx, d.categoriaIcono));
            tvEquipo.setText(d.equipo.nombre);
            tvCodigo.setText("Código: " + valor(d.equipo.codigoActivo));
            tvSerie.setText("Nº Serie: " + valor(d.equipo.numeroSerie));
            tvUbicacion.setText(valor(d.ubicacionNombre));
        };

        EquipoRepository equipos = EquipoRepository.get(ctx);
        if (edicion) {
            tilActivo.setEnabled(false);
            equipos.observarDetalle(args.getLong(ARG_EQUIPO))
                    .observe(getViewLifecycleOwner(), d -> {
                        mostrarEquipo.accept(d);
                        if (d != null) etActivo.setText(d.equipo.codigoActivo);
                    });
        } else {
            MutableLiveData<String> consulta = new MutableLiveData<>("");
            Transformations.switchMap(consulta, texto -> equipos.buscarExacto(texto))
                    .observe(getViewLifecycleOwner(), mostrarEquipo::accept);

            etActivo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    consulta.setValue(s.toString().trim());
                }
            });
            View.OnClickListener buscar = v -> {
                if (equipoSel == null) {
                    tilActivo.setError("No se encontró un activo con ese código o serie");
                }
            };
            tilActivo.setEndIconOnClickListener(buscar);
            etActivo.setOnEditorActionListener((tv, actionId, ev) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    buscar.onClick(tv);
                    return true;
                }
                return false;
            });
        }

        // El fragmento se cierra antes de que termine el envío: el aviso usa el contexto de la app
        Context app = ctx.getApplicationContext();
        Resultado aviso = (ok, mensaje) -> {
            if (!ok) Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
        };
        MantenimientoRepository repo = MantenimientoRepository.get(ctx);

        btnVolver.setOnClickListener(v -> cerrar(v));

        btnGuardar.setOnClickListener(v -> {
            tilActivo.setError(null);
            tilTecnico.setError(null);
            tilDescripcion.setError(null);

            String descripcion = texto(etDescripcion).trim();
            String observaciones = texto(etObservaciones).trim();

            boolean valido = true;
            if (equipoSel == null) {
                tilActivo.setError("Busca un activo válido por código o número de serie");
                valido = false;
            }
            if (tecnicoSelId < 0) {
                tilTecnico.setError("Selecciona un técnico");
                valido = false;
            }
            if (descripcion.length() < 5) {
                tilDescripcion.setError("Describe el trabajo (mínimo 5 caracteres)");
                valido = false;
            }
            if (!valido) return;

            String tipo = tipoActual(chipsTipo);
            String estado = chipsEstado.getCheckedChipId() == R.id.chipTareaCompletado
                    ? EstadoMantenimiento.COMPLETADO : EstadoMantenimiento.EN_PROCESO;
            String obs = observaciones.isEmpty() ? null : observaciones;
            long equipoLocalId = equipoSel.equipo.localId;

            if (edicion) {
                repo.editar(localId, equipoLocalId, tecnicoSelId, tecnicoSelNombre,
                        tipo, estado, fechaSel, descripcion, obs, aviso);
            } else {
                repo.crear(equipoLocalId, tecnicoSelId, tecnicoSelNombre,
                        tipo, estado, fechaSel, descripcion, obs, aviso);
            }
            cerrar(v);
        });
    }

    private void elegirFecha(TextInputEditText etFecha, TextInputLayout tilFecha) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha programada");
        if (fechaSel != null) {
            builder.setSelection(LocalDate.parse(fechaSel)
                    .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        }
        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(seleccion -> {
            fechaSel = Instant.ofEpochMilli(seleccion).atZone(ZoneOffset.UTC)
                    .toLocalDate().toString();
            etFecha.setText(FechaUtil.mostrar(fechaSel));
            tilFecha.setError(null);
        });
        picker.show(getParentFragmentManager(), "fecha");
    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "-" : texto;
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }

    private String tipoActual(ChipGroup chips) {
        int id = chips.getCheckedChipId();
        if (id == R.id.chipTipoCorrectivo) return "CORRECTIVO";
        if (id == R.id.chipTipoIncidencia) return "INCIDENCIA";
        return "PREVENTIVO";
    }

    private int chipTipoDe(String tipo) {
        if ("CORRECTIVO".equals(tipo)) return R.id.chipTipoCorrectivo;
        if ("INCIDENCIA".equals(tipo)) return R.id.chipTipoIncidencia;
        return R.id.chipTipoPreventivo;
    }

    private void cerrar(View v) {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        getParentFragmentManager().popBackStack();
    }
}