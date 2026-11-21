package edu.sv.ues.dam235.equipstock.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import edu.sv.ues.dam235.equipstock.utils.JwtUtils;
import edu.sv.ues.dam235.equipstock.utils.SesionExpirada;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "https://equipstock-api-159159280259.us-central1.run.app/";

    private static Retrofit retrofit;

    public static Retrofit get(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();
            SessionManager session = new SessionManager(appContext);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            boolean depurable = (appContext.getApplicationInfo().flags
                    & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            logging.setLevel(depurable
                    ? HttpLoggingInterceptor.Level.BASIC      // solo la dirección y el código de respuesta
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        okhttp3.Request request = chain.request();
                        String token = session.getToken();
                        // Login y registro no llevan token: no revisar esas rutas
                        boolean esAuth = request.url().encodedPath().startsWith("/api/auth/");

                        // 1. Si el token ya venció, no mandar la petición
                        if (token != null && !esAuth && JwtUtils.estaVencido(token)) {
                            SesionExpirada.manejar(appContext);
                            throw new IOException("Sesión expirada");
                        }

                        okhttp3.Request.Builder builder = request.newBuilder();
                        if (token != null) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        okhttp3.Response response = chain.proceed(builder.build());

                        // 2. El servidor responde 403 con token vencido (y también por permisos),
                        //    así que solo cerramos sesión si el token de verdad venció
                        if ((response.code() == 401 || response.code() == 403)
                                && token != null && !esAuth && JwtUtils.estaVencido(token)) {
                            SesionExpirada.manejar(appContext);
                        }
                        return response;
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}