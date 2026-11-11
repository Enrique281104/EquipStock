package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;
import edu.sv.ues.dam235.equipstock.R;

public final class EstadoEquipo {

    public static final String OPERATIVO = "OPERATIVO";
    public static final String EN_MANTENIMIENTO = "EN_MANTENIMIENTO";
    public static final String INACTIVO = "INACTIVO";

    private EstadoEquipo() { }

    public static String etiqueta(String estado) {
        if (EN_MANTENIMIENTO.equals(estado)) return "En Mantenimiento";
        if (INACTIVO.equals(estado)) return "Inactivo";
        return "Operativo";
    }

    public static int colorFondo(Context ctx, String estado) {
        int id = R.color.estado_operativo_bg;
        if (EN_MANTENIMIENTO.equals(estado)) id = R.color.estado_mantenimiento_bg;
        if (INACTIVO.equals(estado)) id = R.color.estado_inactivo_bg;
        return ContextCompat.getColor(ctx, id);
    }

    public static int colorTexto(Context ctx, String estado) {
        int id = R.color.estado_operativo_text;
        if (EN_MANTENIMIENTO.equals(estado)) id = R.color.estado_mantenimiento_text;
        if (INACTIVO.equals(estado)) id = R.color.estado_inactivo_text;
        return ContextCompat.getColor(ctx, id);
    }
}