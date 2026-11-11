package edu.sv.ues.dam235.equipstock.views.equipment;

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
import edu.sv.ues.dam235.equipstock.database.EquipoConDetalle;
import edu.sv.ues.dam235.equipstock.database.EquipoEntity;
import edu.sv.ues.dam235.equipstock.sync.SyncStatus;
import edu.sv.ues.dam235.equipstock.utils.EstadoEquipo;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;
import java.util.Objects;

public class EquipoAdapter extends ListAdapter<EquipoConDetalle, EquipoAdapter.Holder> {

    public interface Listener {
        void alAbrir(EquipoConDetalle equipo);
    }

    private static final DiffUtil.ItemCallback<EquipoConDetalle> DIFF =
            new DiffUtil.ItemCallback<EquipoConDetalle>() {
                @Override
                public boolean areItemsTheSame(@NonNull EquipoConDetalle a,
                                               @NonNull EquipoConDetalle b) {
                    return a.equipo.localId == b.equipo.localId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull EquipoConDetalle a,
                                                  @NonNull EquipoConDetalle b) {
                    EquipoEntity x = a.equipo;
                    EquipoEntity y = b.equipo;
                    return Objects.equals(x.nombre, y.nombre)
                            && Objects.equals(x.numeroSerie, y.numeroSerie)
                            && Objects.equals(x.codigoActivo, y.codigoActivo)
                            && Objects.equals(x.estado, y.estado)
                            && Objects.equals(x.syncStatus, y.syncStatus)
                            && Objects.equals(a.categoriaNombre, b.categoriaNombre)
                            && Objects.equals(a.categoriaIcono, b.categoriaIcono)
                            && Objects.equals(a.ubicacionNombre, b.ubicacionNombre);
                }
            };

    private final Listener listener;

    public EquipoAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipo, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        EquipoConDetalle item = getItem(position);
        EquipoEntity e = item.equipo;
        Context ctx = h.itemView.getContext();

        h.ivIcono.setImageResource(IconosCategoria.recurso(ctx, item.categoriaIcono));
        h.tvNombre.setText(e.nombre);
        h.tvSerie.setText(ctx.getString(R.string.equipos_serie_codigo,
                e.numeroSerie == null ? "-" : e.numeroSerie,
                e.codigoActivo == null ? "-" : e.codigoActivo));

        String ubicacion = item.ubicacionNombre == null ? "-" : item.ubicacionNombre;
        if (!SyncStatus.SYNCED.equals(e.syncStatus)) {
            ubicacion += " · " + ctx.getString(R.string.equipos_sin_sincronizar);
        }
        h.tvUbicacion.setText(ubicacion);

        h.tvEstado.setText(EstadoEquipo.etiqueta(e.estado));
        h.tvEstado.setTextColor(EstadoEquipo.colorTexto(ctx, e.estado));
        h.tvEstado.setBackgroundTintList(
                ColorStateList.valueOf(EstadoEquipo.colorFondo(ctx, e.estado)));

        h.itemView.setOnClickListener(v -> listener.alAbrir(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView ivIcono;
        final TextView tvNombre;
        final TextView tvSerie;
        final TextView tvUbicacion;
        final TextView tvEstado;

        Holder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.ivIcono);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvSerie = itemView.findViewById(R.id.tvSerie);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}