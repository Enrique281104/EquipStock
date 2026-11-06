package edu.sv.ues.dam235.equipstock.api;

import edu.sv.ues.dam235.equipstock.models.AuthResponse;
import edu.sv.ues.dam235.equipstock.models.LoginRequest;
import edu.sv.ues.dam235.equipstock.models.RegistroRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/registro")
    Call<AuthResponse> registro(@Body RegistroRequest request);
}
