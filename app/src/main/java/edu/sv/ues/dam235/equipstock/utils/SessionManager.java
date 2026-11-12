package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import edu.sv.ues.dam235.equipstock.models.Usuario;

public class SessionManager {

    private static final String PREFS = "equipstock_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROL = "rol";

    private static final String KEY_TELEFONO = "telefono";

    private static final String KEY_FOTO = "foto_actualizada";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void guardarSesion(String token, Usuario u) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_ID, u.getId())
                .putString(KEY_NOMBRE, u.getNombre())
                .putString(KEY_EMAIL, u.getEmail())
                .putString(KEY_ROL, u.getRol())
                .putString(KEY_TELEFONO, u.getTelefono() == null ? "" : u.getTelefono())
                .putLong(KEY_FOTO, u.getFotoActualizada() == null ? 0 : u.getFotoActualizada())
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public long getUserId() { return prefs.getLong(KEY_ID, -1); }
    public String getNombre() { return prefs.getString(KEY_NOMBRE, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getRol() { return prefs.getString(KEY_ROL, ""); }

    public boolean haySesion() { return getToken() != null; }

    // para mostrar u ocultar botones segun el rol
    public boolean esAdmin() { return "ADMIN".equals(getRol()); }

    public void cerrarSesion() { prefs.edit().clear().apply(); }

    public String getTelefono() { return prefs.getString(KEY_TELEFONO, ""); }

    public void actualizarPerfil(String nombre, String telefono) {
        prefs.edit()
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_TELEFONO, telefono == null ? "" : telefono)
                .apply();
    }

    public long getFotoActualizada() { return prefs.getLong(KEY_FOTO, 0); }

    public void setFotoActualizada(long version) {
        prefs.edit().putLong(KEY_FOTO, version).apply();
    }

}