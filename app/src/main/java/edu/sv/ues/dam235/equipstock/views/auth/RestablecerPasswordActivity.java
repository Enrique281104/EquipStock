package edu.sv.ues.dam235.equipstock.views.auth;

import android.content.Intent;
import android.os.Bundle;
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

/** Segunda pantalla: código recibido por correo + nueva contraseña. */
public class RestablecerPasswordActivity extends AppCompatActivity {

    private String email;
    private TextInputLayout tilCodigo, tilNueva, tilConfirmar;
    private TextInputEditText etCodigo, etNueva, etConfirmar;
    private MaterialButton btnCambiar;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restablecer_password);

        // El correo viene de la pantalla anterior
        email = getIntent().getStringExtra(RecuperarPasswordActivity.EXTRA_EMAIL);

        tilCodigo = findViewById(R.id.tilCodigo);
        tilNueva = findViewById(R.id.tilNueva);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etCodigo = findViewById(R.id.etCodigo);
        etNueva = findViewById(R.id.etNueva);
        etConfirmar = findViewById(R.id.etConfirmar);
        btnCambiar = findViewById(R.id.btnCambiar);
        progress = findViewById(R.id.progress);

        btnCambiar.setOnClickListener(v -> cambiarPassword());
    }

    private void cambiarPassword() {
        tilCodigo.setError(null);
        tilNueva.setError(null);
        tilConfirmar.setError(null);

        String codigo = texto(etCodigo);
        String nueva = texto(etNueva);
        String confirmar = texto(etConfirmar);

        // Validaciones antes de llamar a la API
        if (!codigo.matches("\\d{6}")) {
            tilCodigo.setError(getString(R.string.restablecer_error_codigo));
            return;
        }
        if (nueva.length() < 8) { // misma regla que el backend
            tilNueva.setError(getString(R.string.restablecer_error_longitud));
            return;
        }
        if (!nueva.equals(confirmar)) {
            tilConfirmar.setError(getString(R.string.restablecer_error_coinciden));
            return;
        }

        cargando(true);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("codigo", codigo);
        body.put("nueva", nueva);

        ApiClient.get(this).create(RecuperarApi.class).restablecer(body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (isFinishing()) return;
                        cargando(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(RestablecerPasswordActivity.this,
                                    R.string.restablecer_exito, Toast.LENGTH_LONG).show();
                            // Volver al Login cerrando las pantallas de recuperación
                            Intent intent = new Intent(RestablecerPasswordActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RestablecerPasswordActivity.this,
                                    R.string.restablecer_error, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (isFinishing()) return;
                        cargando(false);
                        Toast.makeText(RestablecerPasswordActivity.this,
                                R.string.recuperar_sin_conexion, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private void cargando(boolean activo) {
        progress.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnCambiar.setEnabled(!activo);
    }
}