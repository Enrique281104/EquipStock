package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.UbicacionDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UbicacionApi {

    @GET("api/ubicaciones")
    Call<List<UbicacionDto>> listar();

    @POST("api/ubicaciones")
    Call<UbicacionDto> crear(@Body UbicacionDto dto);

    @PUT("api/ubicaciones/{id}")
    Call<UbicacionDto> actualizar(@Path("id") long id, @Body UbicacionDto dto);

    @DELETE("api/ubicaciones/{id}")
    Call<Void> eliminar(@Path("id") long id);
}