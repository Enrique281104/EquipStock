package edu.sv.ues.dam235.equipstock.views.equipment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.EquipoConDetalle;
import edu.sv.ues.dam235.equipstock.database.EquipoEntity;
import edu.sv.ues.dam235.equipstock.database.MantenimientoEntity;
import edu.sv.ues.dam235.equipstock.repositories.EquipoRepository;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.sync.Resultado;
import edu.sv.ues.dam235.equipstock.utils.EstadoEquipo;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.FechaUtil;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import edu.sv.ues.dam235.equipstock.views.maintenance.MantenimientosFragment;

import android.widget.Toast;

public class EquipoDetalleFragment extends Fragment {

    private static final String ARG_LOCAL_ID = "localId";

    private EquipoConDetalle actual;

    public static EquipoDetalleFragment nueva(long localId) {
        Bundle args = new Bundle();
        args.putLong(ARG_LOCAL_ID, localId);
        EquipoDetalleFragment f = new EquipoDetalleFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipo_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long localId = requireArguments().getLong(ARG_LOCAL_ID);
        boolean esAdmin = new SessionManager(requireContext()).esAdmin();
        EquipoRepository repo = EquipoRepository.get(requireContext());

        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);
        ImageView ivIcono = view.findViewById(R.id.ivIcono);
        TextView tvNombre = view.findViewById(R.id.tvNombre);
        TextView tvEstado = view.findViewById(R.id.tvEstado);
        TextView tvSerie = view.findViewById(R.id.tvSerie);
        TextView tvCodigo = view.findViewById(R.id.tvCodigo);
        TextView tvCategoria = view.findViewById(R.id.tvCategoria);
        TextView tvUbicacion = view.findViewById(R.id.tvUbicacion);

        btnVolver.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Solo el administrador puede editar o eliminar
        btnMenu.setVisibility(esAdmin ? View.VISIBLE : View.INVISIBLE);
        btnMenu.setOnClickListener(v -> mostrarMenu(v, repo, localId));

        repo.observarDetalle(localId).observe(getViewLifecycleOwner(), d -> {
            if (d == null) return;
            actual = d;
            Context ctx = requireContext();
            EquipoEntity e = d.equipo;

            ivIcono.setImageResource(IconosCategoria.recurso(ctx, d.categoriaIcono));
            tvNombre.setText(e.nombre);
            tvEstado.setText(EstadoEquipo.etiqueta(e.estado));
            tvEstado.setTextColor(EstadoEquipo.colorTexto(ctx, e.estado));
            tvEstado.setBackgroundTintList(
                    ColorStateList.valueOf(EstadoEquipo.colorFondo(ctx, e.estado)));
            tvSerie.setText(valor(e.numeroSerie));
            tvCodigo.setText(valor(e.codigoActivo));
            tvCategoria.setText(valor(d.categoriaNombre));
            tvUbicacion.setText(valor(d.ubicacionNombre));
        });

        LinearLayout contenedor = view.findViewById(R.id.historialContainer);
        TextView tvHistorialVacio = view.findViewById(R.id.tvHistorialVacio);
        TextView tvVerHistorial = view.findViewById(R.id.tvVerHistorial);

        MantenimientoRepository.get(requireContext()).ultimosDeEquipo(localId, 3)
                .observe(getViewLifecycleOwner(), lista -> {
                    Context ctx = requireContext();
                    contenedor.removeAllViews();
                    tvHistorialVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    tvVerHistorial.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);

                    for (MantenimientoEntity m : lista) {
                        View fila = getLayoutInflater().inflate(R.layout.item_historial, contenedor, false);
                        TextView estadoView = fila.findViewById(R.id.tvEstado);
                        String estado = EstadoMantenimiento.efectivo(m.estado, m.fechaProgramada);
                        estadoView.setText(EstadoMantenimiento.etiqueta(estado));
                        estadoView.setTextColor(EstadoMantenimiento.colorTexto(ctx, estado));
                        estadoView.setBackgroundTintList(ColorStateList.valueOf(
                                EstadoMantenimiento.colorFondo(ctx, estado)));

                        ((TextView) fila.findViewById(R.id.tvTitulo)).setText(
                                EstadoMantenimiento.tipoCorto(m.tipo) + " - " + valor(m.descripcion));
                        ((TextView) fila.findViewById(R.id.tvDetalle)).setText(
                                FechaUtil.mostrar(m.fechaProgramada) + " • Téc. " + valor(m.tecnicoNombre));
                        contenedor.addView(fila);
                    }
                });

        tvVerHistorial.setOnClickListener(v -> {
            if (actual != null) {
                abrir(MantenimientosFragment.deEquipo(localId, actual.equipo.nombre));
            }
        });

    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "-" : texto;
    }

    private void mostrarMenu(View ancla, EquipoRepository repo, long localId) {
        PopupMenu popup = new PopupMenu(requireContext(), ancla);
        popup.getMenu().add(0, 1, 0, "Editar");
        popup.getMenu().add(0, 2, 1, "Eliminar");
        popup.setOnMenuItemClickListener(item -> {
            if (actual == null) return true;
            if (item.getItemId() == 1) {
                abrir(EquipoFormFragment.editar(actual.equipo));
            } else {
                confirmarEliminar(repo, localId);
            }
            return true;
        });
        popup.show();
    }

    private void confirmarEliminar(EquipoRepository repo, long localId) {
        // El fragmento se cierra antes de que termine el envío, así que el aviso
        // se muestra con el contexto de la aplicación.
        Context app = requireContext().getApplicationContext();
        Resultado aviso = (ok, mensaje) -> {
            if (!ok) Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Eliminar equipo?")
                .setMessage("Se eliminará «" + actual.equipo.nombre
                        + "». Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, w) -> {
                    repo.eliminar(localId, aviso);
                    getParentFragmentManager().popBackStack();
                })
                .show();
    }

    private void abrir(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}