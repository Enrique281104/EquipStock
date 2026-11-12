package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.core.app.NotificationManagerCompat;

/** Ajustes del dispositivo para las notificaciones (comparten archivo con el tema). */
public final class AjustesUtil {

    private static final String PREFS = "equipstock_ajustes";
    private static final String KEY_NOTIF = "notificaciones";
    private static final String KEY_PERMISO_PEDIDO = "permiso_notif_pedido";
    private static final String KEY_FECHA_AVISO = "fecha_ultimo_aviso";

    private AjustesUtil() { }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean notificacionesHabilitadas(Context c) {
        return prefs(c).getBoolean(KEY_NOTIF, true);
    }

    /** Al activar o desactivar se reinicia el límite diario, así se puede volver a probar. */
    public static void setNotificacionesHabilitadas(Context c, boolean habilitadas) {
        prefs(c).edit().putBoolean(KEY_NOTIF, habilitadas).remove(KEY_FECHA_AVISO).apply();
    }

    /** True si el sistema permite mostrar notificaciones de esta app. */
    public static boolean permisoConcedido(Context c) {
        return NotificationManagerCompat.from(c.getApplicationContext()).areNotificationsEnabled();
    }

    public static boolean permisoYaPedido(Context c) {
        return prefs(c).getBoolean(KEY_PERMISO_PEDIDO, false);
    }

    public static void marcarPermisoPedido(Context c) {
        prefs(c).edit().putBoolean(KEY_PERMISO_PEDIDO, true).apply();
    }

    public static String fechaUltimoAviso(Context c) {
        return prefs(c).getString(KEY_FECHA_AVISO, "");
    }

    public static void guardarFechaAviso(Context c, String fecha) {
        prefs(c).edit().putString(KEY_FECHA_AVISO, fecha).apply();
    }

    public static void reiniciarAvisos(Context c) {
        prefs(c).edit().remove(KEY_FECHA_AVISO).apply();
    }
}