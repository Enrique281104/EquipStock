package edu.sv.ues.dam235.equipstock.views.maintenance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.MantenimientoConDetalle;
import edu.sv.ues.dam235.equipstock.database.MantenimientoEntity;
import edu.sv.ues.dam235.equipstock.sync.SyncStatus;
import edu.sv.ues.dam235.equipstock.utils.EstadoMantenimiento;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;
import java.util.Objects;

public class MantenimientoAdapter
        extends ListAdapter<MantenimientoConDetalle, MantenimientoAdapter.Holder> {

    public interface Listener {
        void alAbrir(MantenimientoConDetalle mantenimiento);
    }

    private static final DiffUtil.ItemCallback<MantenimientoConDetalle> DIFF =
            new DiffUtil.ItemCallback<MantenimientoConDetalle>() {
                @Override
                public boolean areItemsTheSame(@NonNull MantenimientoConDetalle a,
                                               @NonNull MantenimientoConDetalle b) {
                    return a.mantenimiento.localId == b.mantenimiento.localId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull MantenimientoConDetalle a,
                                                  @NonNull MantenimientoConDetalle b) {
                    MantenimientoEntity x = a.mantenimiento;
                    MantenimientoEntity y = b.mantenimiento;
                    return Objects.equals(x.estado, y.estado)
                            && Objects.equals(x.fechaProgramada, y.fechaProgramada)
                            && Objects.equals(x.tecnicoNombre, y.tecnicoNombre)
                            && Objects.equals(x.syncStatus, y.syncStatus)
                            && Objects.equals(a.equipoNombre, b.equipoNombre)
                            && Objects.equals(a.equipoSerie, b.equipoSerie)
                            && Objects.equals(a.equipoCodigo, b.equipoCodigo)
                            && Objects.equals(a.equipoCategoriaIcono, b.equipoCategoriaIcono)
                            && Objects.equals(a.equipoUbicacion, b.equipoUbicacion);
                }
            };

    private final Listener listener;

    public MantenimientoAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mantenimiento, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        MantenimientoConDetalle item = getItem(position);
        MantenimientoEntity m = item.mantenimiento;
        Context ctx = h.itemView.getContext();

        h.ivIcono.setImageResource(IconosCategoria.recurso(ctx, item.equipoCategoriaIcono));
        h.tvNombre.setText(item.equipoNombre == null ? "-" : item.equipoNombre);
        h.tvSerie.setText(ctx.getString(R.string.equipos_serie_codigo,
                item.equipoSerie == null ? "-" : item.equipoSerie,
                item.equipoCodigo == null ? "-" : item.equipoCodigo));
        h.tvUbicacion.setText(item.equipoUbicacion == null ? "-" : item.equipoUbicacion);

        String tecnico = m.tecnicoNombre == null ? "-" : m.tecnicoNombre;
        if (!SyncStatus.SYNCED.equals(m.syncStatus)) {
            tecnico += " · " + ctx.getString(R.string.mantenimiento_sin_sincronizar);
        }
        h.tvTecnico.setText(tecnico);

        String estado = EstadoMantenimiento.efectivo(m.estado, m.fechaProgramada);
        h.tvEstado.setText(EstadoMantenimiento.etiqueta(estado));
        h.tvEstado.setTextColor(EstadoMantenimiento.colorTexto(ctx, estado));
        h.tvEstado.setBackgroundTintList(
                ColorStateList.valueOf(EstadoMantenimiento.colorFondo(ctx, estado)));

        h.itemView.setOnClickListener(v -> listener.alAbrir(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView ivIcono;
        final TextView tvNombre;
        final TextView tvSerie;
        final TextView tvUbicacion;
        final TextView tvTecnico;
        final TextView tvEstado;

        Holder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.ivIcono);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvSerie = itemView.findViewById(R.id.tvSerie);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            tvTecnico = itemView.findViewById(R.id.tvTecnico);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}