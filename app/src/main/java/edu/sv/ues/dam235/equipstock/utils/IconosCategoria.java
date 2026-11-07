package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import edu.sv.ues.dam235.equipstock.R;

public final class IconosCategoria {

    // Mismo orden que la cuadrícula del formulario (3 filas de 4)
    public static final String[] NOMBRES = {
            "cat_escritorio", "cat_laptop", "cat_mouse", "cat_disco",
            "cat_servidor", "cat_proyector", "cat_impresora", "cat_tablet",
            "cat_ups", "cat_audifonos", "cat_teclado"
    };

    private IconosCategoria() { }

    /** Devuelve el drawable del nombre guardado, o uno por defecto si no existe. */
    public static int recurso(Context context, String nombre) {
        if (nombre != null) {
            int id = context.getResources()
                    .getIdentifier(nombre, "drawable", context.getPackageName());
            if (id != 0) return id;
        }
        return R.drawable.ic_nav_categorias;
    }
}