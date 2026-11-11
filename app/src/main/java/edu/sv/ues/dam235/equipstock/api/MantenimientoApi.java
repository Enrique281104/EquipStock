package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.MantenimientoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MantenimientoApi {

    @GET("api/mantenimientos")
    Call<List<MantenimientoDto>> listar();

    @POST("api/mantenimientos")
    Call<MantenimientoDto> crear(@Body MantenimientoDto dto);

    @PUT("api/mantenimientos/{id}")
    Call<MantenimientoDto> actualizar(@Path("id") long id, @Body MantenimientoDto dto);
}