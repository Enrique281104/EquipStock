package edu.sv.ues.dam235.equipstock.views.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
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
import edu.sv.ues.dam235.equipstock.models.LoginRequest;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnIngresar, btnRegistro;
    private ProgressBar progress;
    private SessionManager session;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (session.haySesion()) {
            irAlInicio();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnRegistro = findViewById(R.id.btnRegistro);
        progress = findViewById(R.id.progress);

        authApi = ApiClient.get(this).create(AuthApi.class);

        btnIngresar.setOnClickListener(v -> intentarLogin());
        btnRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarLogin() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean valido = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo válido");
            valido = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Ingresa tu contraseña");
            valido = false;
        }
        if (!valido) return;

        setCargando(true);
        authApi.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setCargando(false);
                if (response.isSuccessful() && response.body() != null) {
                    session.guardarSesion(response.body().getToken(), response.body().getUsuario());
                    irAlInicio();
                } else if (response.code() == 401) {
                    Toast.makeText(LoginActivity.this,
                            "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error del servidor (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setCargando(false);
                Toast.makeText(LoginActivity.this,
                        "No se pudo conectar. Revisa tu conexión.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnIngresar.setEnabled(!cargando);
        btnRegistro.setEnabled(!cargando);
    }

    private void irAlInicio() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}