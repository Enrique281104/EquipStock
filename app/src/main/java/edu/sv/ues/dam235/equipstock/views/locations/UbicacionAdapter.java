package edu.sv.ues.dam235.equipstock.views.locations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.UbicacionEntity;
import edu.sv.ues.dam235.equipstock.sync.SyncStatus;
import java.util.Objects;

public class UbicacionAdapter extends ListAdapter<UbicacionEntity, UbicacionAdapter.Holder> {

    public interface Listener {
        void alAbrir(UbicacionEntity ubicacion);
    }

    private static final DiffUtil.ItemCallback<UbicacionEntity> DIFF =
            new DiffUtil.ItemCallback<UbicacionEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull UbicacionEntity a,
                                               @NonNull UbicacionEntity b) {
                    return a.localId == b.localId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull UbicacionEntity a,
                                                  @NonNull UbicacionEntity b) {
                    return Objects.equals(a.nombre, b.nombre)
                            && Objects.equals(a.descripcion, b.descripcion)
                            && a.equiposVinculados == b.equiposVinculados
                            && Objects.equals(a.syncStatus, b.syncStatus);
                }
            };

    private final Listener listener;

    public UbicacionAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ubicacion, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        UbicacionEntity u = getItem(position);
        Context ctx = h.itemView.getContext();

        h.tvNombre.setText(u.nombre);

        boolean hayDescripcion = u.descripcion != null && !u.descripcion.isEmpty();
        h.tvDescripcion.setText(u.descripcion);
        h.tvDescripcion.setVisibility(hayDescripcion ? View.VISIBLE : View.GONE);

        String subtitulo = ctx.getResources().getQuantityString(
                R.plurals.equipos_asignados, u.equiposVinculados, u.equiposVinculados);
        if (!SyncStatus.SYNCED.equals(u.syncStatus)) {
            subtitulo += " · " + ctx.getString(R.string.ubicaciones_sin_sincronizar);
        }
        h.tvSubtitulo.setText(subtitulo);

        h.itemView.setOnClickListener(v -> listener.alAbrir(u));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvNombre;
        final TextView tvDescripcion;
        final TextView tvSubtitulo;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvSubtitulo = itemView.findViewById(R.id.tvSubtitulo);
        }
    }
}