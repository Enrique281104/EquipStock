package edu.sv.ues.dam235.equipstock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.sync.SyncScheduler;
import edu.sv.ues.dam235.equipstock.utils.AjustesUtil;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import edu.sv.ues.dam235.equipstock.views.PerfilFragment;
import edu.sv.ues.dam235.equipstock.views.auth.LoginActivity;
import edu.sv.ues.dam235.equipstock.views.categories.CategoriasFragment;
import edu.sv.ues.dam235.equipstock.views.dashboard.DashboardFragment;
import edu.sv.ues.dam235.equipstock.views.equipment.EquiposFragment;
import edu.sv.ues.dam235.equipstock.views.maintenance.MantenimientosFragment;

public class MainActivity extends AppCompatActivity {

    // El resultado no cambia nada aquí: Configuración refleja el estado real del permiso
    private final ActivityResultLauncher<String> pedirPermisoNotificaciones =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    concedido -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (!session.haySesion()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

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

        // Sincronización: automática al volver el internet y cada 30 minutos en segundo plano
        SyncManager.get(this);
        SyncScheduler.programar(this);
        pedirPermisoDeNotificaciones();
    }

    private void pedirPermisoDeNotificaciones() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (!AjustesUtil.notificacionesHabilitadas(this) || AjustesUtil.permisoYaPedido(this)) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;

        AjustesUtil.marcarPermisoPedido(this);
        pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private Fragment fragmentPara(int id) {
        if (id == R.id.nav_equipos) return new EquiposFragment();
        if (id == R.id.nav_mantenimientos) return new MantenimientosFragment();
        if (id == R.id.nav_categorias) return new CategoriasFragment();
        if (id == R.id.nav_perfil) return new PerfilFragment();
        return new DashboardFragment();
    }

    private void mostrar(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}