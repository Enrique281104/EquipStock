package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.TecnicoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TecnicoApi {

    @GET("api/usuarios/tecnicos")
    Call<List<TecnicoDto>> listar();
}