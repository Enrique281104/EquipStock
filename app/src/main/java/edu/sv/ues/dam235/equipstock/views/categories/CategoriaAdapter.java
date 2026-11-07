package edu.sv.ues.dam235.equipstock.views.categories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.CategoriaEntity;
import edu.sv.ues.dam235.equipstock.sync.SyncStatus;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;
import java.util.Objects;

public class CategoriaAdapter extends ListAdapter<CategoriaEntity, CategoriaAdapter.Holder> {

    public interface Listener {
        void alEditar(CategoriaEntity categoria);
    }

    private static final DiffUtil.ItemCallback<CategoriaEntity> DIFF =
            new DiffUtil.ItemCallback<CategoriaEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull CategoriaEntity a,
                                               @NonNull CategoriaEntity b) {
                    return a.localId == b.localId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull CategoriaEntity a,
                                                  @NonNull CategoriaEntity b) {
                    return Objects.equals(a.nombre, b.nombre)
                            && Objects.equals(a.descripcion, b.descripcion)
                            && Objects.equals(a.icono, b.icono)
                            && a.equiposVinculados == b.equiposVinculados
                            && Objects.equals(a.syncStatus, b.syncStatus);
                }
            };

    private final Listener listener;
    private final boolean puedeEditar;

    public CategoriaAdapter(Listener listener, boolean puedeEditar) {
        super(DIFF);
        this.listener = listener;
        this.puedeEditar = puedeEditar;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        CategoriaEntity c = getItem(position);
        Context ctx = h.itemView.getContext();

        h.ivIcono.setImageResource(IconosCategoria.recurso(ctx, c.icono));
        h.tvNombre.setText(c.nombre);

        String subtitulo = ctx.getResources().getQuantityString(
                R.plurals.equipos_vinculados, c.equiposVinculados, c.equiposVinculados);
        if (!SyncStatus.SYNCED.equals(c.syncStatus)) {
            subtitulo += " · " + ctx.getString(R.string.categorias_sin_sincronizar);
        }
        h.tvSubtitulo.setText(subtitulo);

        h.btnEditar.setVisibility(puedeEditar ? View.VISIBLE : View.GONE);
        h.btnEditar.setOnClickListener(v -> listener.alEditar(c));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView ivIcono;
        final TextView tvNombre;
        final TextView tvSubtitulo;
        final ImageButton btnEditar;

        Holder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.ivIcono);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvSubtitulo = itemView.findViewById(R.id.tvSubtitulo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }
    }
}