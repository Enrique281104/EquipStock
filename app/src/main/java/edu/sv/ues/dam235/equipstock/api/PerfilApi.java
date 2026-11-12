package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.PasswordRequest;
import edu.sv.ues.dam235.equipstock.models.PerfilRequest;
import edu.sv.ues.dam235.equipstock.models.Usuario;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface PerfilApi {

    @GET("api/perfil")
    Call<Usuario> obtener();

    @PUT("api/perfil")
    Call<Usuario> actualizar(@Body PerfilRequest body);

    @PUT("api/perfil/password")
    Call<Void> cambiarPassword(@Body PasswordRequest body);

    @PUT("api/perfil/foto")
    Call<Usuario> subirFoto(@Body RequestBody cuerpo);

    @DELETE("api/perfil/foto")
    Call<Usuario> eliminarFoto();

    @GET("api/perfil/foto")
    Call<ResponseBody> descargarFoto();
}