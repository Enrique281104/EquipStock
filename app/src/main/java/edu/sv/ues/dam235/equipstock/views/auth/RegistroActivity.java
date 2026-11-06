package edu.sv.ues.dam235.equipstock.views.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sv.ues.dam235.equipstock.MainActivity;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.AuthApi;
import edu.sv.ues.dam235.equipstock.models.AuthResponse;
import edu.sv.ues.dam235.equipstock.models.RegistroRequest;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilEmail, tilPassword, tilConfirmar;
    private TextInputEditText etNombre, etEmail, etPassword, etConfirmar;
    private MaterialButton btnRegistrar;
    private TextView tvYaTengoCuenta;
    private ProgressBar progress;
    private SessionManager session;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        session = new SessionManager(this);
        authApi = ApiClient.get(this).create(AuthApi.class);

        tilNombre = findViewById(R.id.tilNombre);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmar = findViewById(R.id.etConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvYaTengoCuenta = findViewById(R.id.tvYaTengoCuenta);
        progress = findViewById(R.id.progress);

        btnRegistrar.setOnClickListener(v -> intentarRegistro());
        tvYaTengoCuenta.setOnClickListener(v -> finish());
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }

    private void intentarRegistro() {
        String nombre = texto(etNombre).trim();
        String email = texto(etEmail).trim();
        String password = texto(etPassword);
        String confirmar = texto(etConfirmar);

        tilNombre.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmar.setError(null);

        boolean valido = true;
        if (nombre.length() < 3) {
            tilNombre.setError("Ingresa tu nombre");
            valido = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo válido");
            valido = false;
        }
        if (password.length() < 8) {
            tilPassword.setError("Mínimo 8 caracteres");
            valido = false;
        }
        if (!password.equals(confirmar)) {
            tilConfirmar.setError("Las contraseñas no coinciden");
            valido = false;
        }
        if (!valido) return;

        setCargando(true);
        authApi.registro(new RegistroRequest(nombre, email, password, null))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setCargando(false);
                        if (response.isSuccessful() && response.body() != null) {
                            session.guardarSesion(response.body().getToken(),
                                    response.body().getUsuario());
                            Intent i = new Intent(RegistroActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        } else if (response.code() == 400) {
                            Toast.makeText(RegistroActivity.this,
                                    mensajeDeError(response), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegistroActivity.this,
                                    "Error del servidor (" + response.code() + ")",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setCargando(false);
                        Toast.makeText(RegistroActivity.this,
                                "No se pudo conectar. Revisa tu conexión.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String mensajeDeError(Response<AuthResponse> response) {
        try {
            if (response.errorBody() != null) {
                String msg = new JSONObject(response.errorBody().string()).optString("error", "");
                if (!msg.isEmpty() && !msg.equals("Bad Request")) return msg;
            }
        } catch (Exception ignored) { }
        return "Datos inválidos. Revisa el formulario.";
    }

    private void setCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!cargando);
    }
}