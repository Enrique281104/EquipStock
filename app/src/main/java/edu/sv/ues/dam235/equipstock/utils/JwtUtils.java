package edu.sv.ues.dam235.equipstock.utils;

import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Lee la fecha de vencimiento ("exp") que viene dentro del token JWT.
 * No verifica la firma (eso lo hace el servidor); solo sirve para saber
 * si el token ya venció sin tener que llamar a la API.
 */
public final class JwtUtils {

    private JwtUtils() { }

    public static boolean estaVencido(String token) {
        if (token == null || token.isEmpty()) return true;
        try {
            String[] partes = token.split("\\.");
            if (partes.length < 2) return true;

            byte[] bytes = Base64.decode(partes[1],
                    Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            JSONObject payload = new JSONObject(new String(bytes, StandardCharsets.UTF_8));

            long expSegundos = payload.optLong("exp", 0);
            if (expSegundos == 0) return false; // sin fecha de vencimiento

            return System.currentTimeMillis() >= expSegundos * 1000L;
        } catch (Exception e) {
            return true; // token dañado: tratarlo como vencido
        }
    }
}