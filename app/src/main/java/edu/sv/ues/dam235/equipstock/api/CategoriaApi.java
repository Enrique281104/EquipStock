package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.CategoriaDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoriaApi {

    @GET("api/categorias")
    Call<List<CategoriaDto>> listar();

    @POST("api/categorias")
    Call<CategoriaDto> crear(@Body CategoriaDto dto);

    @PUT("api/categorias/{id}")
    Call<CategoriaDto> actualizar(@Path("id") long id, @Body CategoriaDto dto);

    @DELETE("api/categorias/{id}")
    Call<Void> eliminar(@Path("id") long id);
}