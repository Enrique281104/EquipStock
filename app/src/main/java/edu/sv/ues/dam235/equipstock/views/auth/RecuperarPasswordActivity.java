package edu.sv.ues.dam235.equipstock.views.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.RecuperarApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Pantalla 2.2: el usuario escribe su correo para recibir el código. */
public class RecuperarPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnEnviar;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_password);

        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnEnviar = findViewById(R.id.btnEnviarCodigo);
        progress = findViewById(R.id.progress);

        btnEnviar.setOnClickListener(v -> enviarCodigo());
    }

    private void enviarCodigo() {
        tilEmail.setError(null);
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.recuperar_error_email));
            return;
        }

        cargando(true);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        ApiClient.get(this).create(RecuperarApi.class).recuperar(body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (isFinishing()) return;
                        cargando(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(RecuperarPasswordActivity.this,
                                    R.string.recuperar_codigo_enviado, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RecuperarPasswordActivity.this,
                                    RestablecerPasswordActivity.class);
                            intent.putExtra(EXTRA_EMAIL, email);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RecuperarPasswordActivity.this,
                                    R.string.recuperar_error_envio, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (isFinishing()) return;
                        cargando(false);
                        Toast.makeText(RecuperarPasswordActivity.this,
                                R.string.recuperar_sin_conexion, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cargando(boolean activo) {
        progress.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnEnviar.setEnabled(!activo);
    }
}