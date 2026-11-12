package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

public final class TemaUtil {

    private static final String PREFS = "equipstock_ajustes";
    private static final String KEY_MODO = "modo_noche";   // -1 sistema, 0 claro, 1 oscuro

    private TemaUtil() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Aplica el tema guardado. Sin preferencia, la app sigue al sistema. */
    public static void aplicar(Context context) {
        int modo = prefs(context).getInt(KEY_MODO, -1);
        AppCompatDelegate.setDefaultNightMode(modo == 1
                ? AppCompatDelegate.MODE_NIGHT_YES
                : modo == 0
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setOscuro(Context context, boolean oscuro) {
        prefs(context).edit().putInt(KEY_MODO, oscuro ? 1 : 0).apply();
        aplicar(context);
    }

    /** Si la pantalla se está viendo en modo oscuro ahora mismo. */
    public static boolean esOscuroActivo(Context context) {
        int modo = prefs(context).getInt(KEY_MODO, -1);
        if (modo == 1) return true;
        if (modo == 0) return false;
        int mascara = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mascara == Configuration.UI_MODE_NIGHT_YES;
    }
}