package edu.sv.ues.dam235.equipstock.api;

import android.content.Context;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
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
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        okhttp3.Request.Builder builder = chain.request().newBuilder();
                        String token = session.getToken();
                        if (token != null) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
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