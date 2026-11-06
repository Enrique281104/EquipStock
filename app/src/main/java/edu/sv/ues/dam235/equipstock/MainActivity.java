package edu.sv.ues.dam235.equipstock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.sv.ues.dam235.equipstock.utils.SessionManager;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SessionManager s = new SessionManager(this);
        Toast.makeText(this, "Hola " + s.getNombre() + " (" + s.getRol() + ")",
                Toast.LENGTH_LONG).show();
        findViewById(R.id.main).setOnLongClickListener(v -> {
            s.cerrarSesion();
            startActivity(new Intent(this, edu.sv.ues.dam235.equipstock.views.auth.LoginActivity.class));
            finish();
            return true;
        });

    }
}