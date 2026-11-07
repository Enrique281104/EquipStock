package edu.sv.ues.dam235.equipstock.views.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.utils.IconosCategoria;

public class IconoAdapter extends RecyclerView.Adapter<IconoAdapter.Holder> {

    private final String[] nombres;
    private int seleccionado = -1;

    public IconoAdapter(String[] nombres, String inicial) {
        this.nombres = nombres;
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equals(inicial)) seleccionado = i;
        }
    }

    /** Nombre del ícono elegido, o null si no eligió ninguno. */
    public String getSeleccionado() {
        return seleccionado < 0 ? null : nombres[seleccionado];
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icono, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.ivIcono.setImageResource(
                IconosCategoria.recurso(h.itemView.getContext(), nombres[position]));
        h.tile.setSelected(position == seleccionado);
        h.itemView.setOnClickListener(v -> {
            int anterior = seleccionado;
            seleccionado = h.getBindingAdapterPosition();
            if (anterior >= 0) notifyItemChanged(anterior);
            notifyItemChanged(seleccionado);
        });
    }

    @Override
    public int getItemCount() {
        return nombres.length;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final View tile;
        final ImageView ivIcono;

        Holder(@NonNull View itemView) {
            super(itemView);
            tile = itemView.findViewById(R.id.tile);
            ivIcono = itemView.findViewById(R.id.ivIcono);
        }
    }
}