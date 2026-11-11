package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.EquipoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EquipoApi {

    @GET("api/equipos")
    Call<List<EquipoDto>> listar();

    @POST("api/equipos")
    Call<EquipoDto> crear(@Body EquipoDto dto);

    @PUT("api/equipos/{id}")
    Call<EquipoDto> actualizar(@Path("id") long id, @Body EquipoDto dto);

    @DELETE("api/equipos/{id}")
    Call<Void> eliminar(@Path("id") long id);
}