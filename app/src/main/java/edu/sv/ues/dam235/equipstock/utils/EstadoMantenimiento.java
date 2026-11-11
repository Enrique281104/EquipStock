package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;
import edu.sv.ues.dam235.equipstock.R;

public final class EstadoMantenimiento {

    public static final String COMPLETADO = "COMPLETADO";
    public static final String EN_PROCESO = "EN_PROCESO";
    public static final String VENCIDO = "VENCIDO";

    private EstadoMantenimiento() { }

    /** Estado que se muestra: En Proceso con fecha anterior a hoy es Vencido. */
    public static String efectivo(String estado, String fechaProgramada) {
        if (EN_PROCESO.equals(estado) && fechaProgramada != null
                && fechaProgramada.compareTo(FechaUtil.hoy()) < 0) {
            return VENCIDO;
        }
        return estado;
    }

    public static String etiqueta(String estado) {
        if (COMPLETADO.equals(estado)) return "Completado";
        if (VENCIDO.equals(estado)) return "Vencido";
        return "En Proceso";
    }

    public static String etiquetaTipo(String tipo) {
        if ("CORRECTIVO".equals(tipo)) return "Mantenimiento Correctivo";
        if ("INCIDENCIA".equals(tipo)) return "Incidencia";
        return "Mantenimiento Preventivo";
    }

    public static String tipoCorto(String tipo) {
        if ("CORRECTIVO".equals(tipo)) return "Correctivo";
        if ("INCIDENCIA".equals(tipo)) return "Incidencia";
        return "Preventivo";
    }

    public static int colorFondo(Context ctx, String estado) {
        int id = R.color.estado_mantenimiento_bg;                 // En Proceso: naranja
        if (COMPLETADO.equals(estado)) id = R.color.estado_operativo_bg;   // verde
        if (VENCIDO.equals(estado)) id = R.color.estado_inactivo_bg;       // rosado
        return ContextCompat.getColor(ctx, id);
    }

    public static int colorTexto(Context ctx, String estado) {
        int id = R.color.estado_mantenimiento_text;
        if (COMPLETADO.equals(estado)) id = R.color.estado_operativo_text;
        if (VENCIDO.equals(estado)) id = R.color.estado_inactivo_text;
        return ContextCompat.getColor(ctx, id);
    }
}