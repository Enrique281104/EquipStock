package edu.sv.ues.dam235.equipstock.views.maintenance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.MantenimientoConDetalle;
import edu.sv.ues.dam235.equipstock.database.MantenimientoEntity;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.FechaUtil;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;

public class MantenimientoDetalleFragment extends Fragment {

    private static final String ARG_LOCAL_ID = "localId";

    private MantenimientoConDetalle actual;

    public static MantenimientoDetalleFragment nueva(long localId) {
        Bundle args = new Bundle();
        args.putLong(ARG_LOCAL_ID, localId);
        MantenimientoDetalleFragment f = new MantenimientoDetalleFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mantenimiento_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long localId = requireArguments().getLong(ARG_LOCAL_ID);
        SessionManager session = new SessionManager(requireContext());
        MantenimientoRepository repo = MantenimientoRepository.get(requireContext());

        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        ImageButton btnEditar = view.findViewById(R.id.btnEditar);
        MaterialButton btnActualizar = view.findViewById(R.id.btnActualizar);
        ImageView ivIcono = view.findViewById(R.id.ivIcono);
        TextView tvEquipo = view.findViewById(R.id.tvEquipo);
        TextView tvCodigo = view.findViewById(R.id.tvCodigo);
        TextView tvSerie = view.findViewById(R.id.tvSerie);
        TextView tvUbicacion = view.findViewById(R.id.tvUbicacion);
        TextView tvTipo = view.findViewById(R.id.tvTipo);
        TextView tvEstado = view.findViewById(R.id.tvEstado);
        TextView tvFecha = view.findViewById(R.id.tvFecha);
        TextView tvTecnico = view.findViewById(R.id.tvTecnico);
        TextView tvDescripcion = view.findViewById(R.id.tvDescripcion);
        TextView tvObsTitulo = view.findViewById(R.id.tvObservacionesTitulo);
        TextView tvObs = view.findViewById(R.id.tvObservaciones);

        btnVolver.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnEditar.setOnClickListener(v -> {
            if (actual == null) return;
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer,
                            MantenimientoFormFragment.editar(actual.mantenimiento))
                    .addToBackStack(null)
                    .commit();
        });
        btnActualizar.setOnClickListener(v -> elegirEstado(repo, localId));

        repo.observarDetalle(localId).observe(getViewLifecycleOwner(), d -> {
            if (d == null) return;
            actual = d;
            Context ctx = requireContext();
            MantenimientoEntity m = d.mantenimiento;

            ivIcono.setImageResource(IconosCategoria.recurso(ctx, d.equipoCategoriaIcono));
            tvEquipo.setText(d.equipoNombre == null ? "-" : d.equipoNombre);
            tvCodigo.setText("Código: " + valor(d.equipoCodigo));
            tvSerie.setText("Nº Serie: " + valor(d.equipoSerie));
            tvUbicacion.setText(valor(d.equipoUbicacion));

            tvTipo.setText(EstadoMantenimiento.etiquetaTipo(m.tipo));
            String estado = EstadoMantenimiento.efectivo(m.estado, m.fechaProgramada);
            tvEstado.setText(EstadoMantenimiento.etiqueta(estado));
            tvEstado.setTextColor(EstadoMantenimiento.colorTexto(ctx, estado));
            tvEstado.setBackgroundTintList(
                    ColorStateList.valueOf(EstadoMantenimiento.colorFondo(ctx, estado)));
            tvFecha.setText(FechaUtil.mostrar(m.fechaProgramada));
            tvTecnico.setText(valor(m.tecnicoNombre));
            tvDescripcion.setText(valor(m.descripcion));

            boolean hayObs = m.observaciones != null && !m.observaciones.isEmpty();
            tvObsTitulo.setVisibility(hayObs ? View.VISIBLE : View.GONE);
            tvObs.setVisibility(hayObs ? View.VISIBLE : View.GONE);
            tvObs.setText(m.observaciones);

            // El admin puede con todos; el técnico solo con los suyos
            boolean puede = session.esAdmin()
                    || (m.tecnicoId != null && m.tecnicoId == session.getUserId());
            btnEditar.setVisibility(puede ? View.VISIBLE : View.INVISIBLE);
            btnActualizar.setVisibility(puede ? View.VISIBLE : View.GONE);
        });
    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "-" : texto;
    }

    private void elegirEstado(MantenimientoRepository repo, long localId) {
        if (actual == null) return;
        // El aviso se muestra con el contexto de la aplicación por si la pantalla se cierra
        Context app = requireContext().getApplicationContext();
        Resultado aviso = (ok, mensaje) -> {
            if (!ok) Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
        };

        String[] opciones = {"En Proceso", "Completado"};
        int seleccion = EstadoMantenimiento.COMPLETADO.equals(actual.mantenimiento.estado) ? 1 : 0;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Actualizar estado")
                .setSingleChoiceItems(opciones, seleccion, (dialog, which) -> {
                    String nuevo = which == 1
                            ? EstadoMantenimiento.COMPLETADO : EstadoMantenimiento.EN_PROCESO;
                    if (!nuevo.equals(actual.mantenimiento.estado)) {
                        repo.cambiarEstado(localId, nuevo, aviso);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}