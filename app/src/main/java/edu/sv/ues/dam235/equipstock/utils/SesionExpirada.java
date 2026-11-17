package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.views.auth.LoginActivity;

/** Cierra la sesión y lleva al Login cuando el token venció. */
public final class SesionExpirada {

    // Evita abrir el Login varias veces si fallan varias peticiones a la vez
    private static final AtomicBoolean enCurso = new AtomicBoolean(false);

    private SesionExpirada() { }

    public static void manejar(Context context) {
        if (!enCurso.compareAndSet(false, true)) return;

        Context appContext = context.getApplicationContext();
        new SessionManager(appContext).cerrarSesion(); // no borra los datos locales

        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(appContext, R.string.sesion_expirada, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);

            enCurso.set(false);
        });
    }
}