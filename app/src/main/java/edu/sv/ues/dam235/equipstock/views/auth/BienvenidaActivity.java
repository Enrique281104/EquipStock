package edu.sv.ues.dam235.equipstock.views.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import edu.sv.ues.dam235.equipstock.MainActivity;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;

public class BienvenidaActivity extends AppCompatActivity {

    private static final long DURACION_LAUNCH_MS = 2000;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Launch 1: logo grande sobre verde
        setContentView(R.layout.activity_launch);

        handler.postDelayed(this::continuar, DURACION_LAUNCH_MS);
    }

    private void continuar() {
        // Si ya hay sesión, ir directo al inicio
        if (new SessionManager(this).haySesion()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        mostrarBienvenida();
    }

    // Launch 1.1: bienvenida con los botones
    private void mostrarBienvenida() {
        setContentView(R.layout.activity_bienvenida);

        findViewById(R.id.btnIniciarSesion).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btnRegistrarse).setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));

        findViewById(R.id.txtOlvidaste).setOnClickListener(v ->
                startActivity(new Intent(this, RecuperarPasswordActivity.class)));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null); // evita errores si se cierra antes de tiempo
        super.onDestroy();
    }
}