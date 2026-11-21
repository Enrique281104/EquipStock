package edu.sv.ues.dam235.equipstock.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Rutas para recuperar la contraseña (las implementa Enrique en el backend). */
public interface RecuperarApi {

    // Body: { "email": "..." }
    @POST("api/auth/recuperar")
    Call<Void> recuperar(@Body Map<String, String> body);

    // Body: { "email": "...", "codigo": "123456", "nueva": "..." }
    @POST("api/auth/restablecer")
    Call<Void> restablecer(@Body Map<String, String> body);
}