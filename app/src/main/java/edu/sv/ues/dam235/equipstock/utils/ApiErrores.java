package edu.sv.ues.dam235.equipstock.utils;

import org.json.JSONObject;
import retrofit2.Response;

public final class ApiErrores {

    private ApiErrores() { }

    /** Mensaje del campo "error" que devuelve la API, o null si no hay uno útil. */
    public static String mensaje(Response<?> respuesta) {
        try {
            if (respuesta.errorBody() != null) {
                String msg = new JSONObject(respuesta.errorBody().string()).optString("error", "");
                if (!msg.isEmpty() && !msg.equals("Bad Request")) return msg;
            }
        } catch (Exception ignored) { }
        return null;
    }
}