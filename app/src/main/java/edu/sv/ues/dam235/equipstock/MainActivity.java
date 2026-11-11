package edu.sv.ues.dam235.equipstock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.sv.ues.dam235.equipstock.repositories.CategoriaRepository;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.repositories.TecnicoRepository;
import edu.sv.ues.dam235.equipstock.repositories.UbicacionRepository;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import edu.sv.ues.dam235.equipstock.views.PerfilFragment;
import edu.sv.ues.dam235.equipstock.views.PlaceholderFragment;
import edu.sv.ues.dam235.equipstock.views.auth.LoginActivity;
import edu.sv.ues.dam235.equipstock.views.categories.CategoriasFragment;
import edu.sv.ues.dam235.equipstock.views.equipment.EquiposFragment;
import edu.sv.ues.dam235.equipstock.views.maintenance.MantenimientosFragment;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.haySesion()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        SyncManager.get(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            mostrar(fragmentPara(item.getItemId()));
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // TEMPORAL: mantener presionada la pantalla cierra sesion (falta flujo de logout))
        findViewById(R.id.fragmentContainer).setOnLongClickListener(v -> {
            session.cerrarSesion();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        });
    }

    private Fragment fragmentPara(int id) {
        if (id == R.id.nav_equipos) return new EquiposFragment();
        if (id == R.id.nav_mantenimientos) return new MantenimientosFragment();
        if (id == R.id.nav_categorias) return new CategoriasFragment();
        if (id == R.id.nav_perfil) return new PerfilFragment();
        return PlaceholderFragment.nueva("Inicio");
    }

    private void mostrar(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}