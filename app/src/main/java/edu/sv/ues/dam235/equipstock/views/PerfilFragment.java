package edu.sv.ues.dam235.equipstock.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.sync.PendientesLiveData;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.utils.FotoUtil;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import edu.sv.ues.dam235.equipstock.views.auth.LoginActivity;
import edu.sv.ues.dam235.equipstock.views.locations.UbicacionesFragment;

public class PerfilFragment extends Fragment {

    private int pendientes = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        TextView tvInicial = view.findViewById(R.id.tvInicial);
        TextView tvNombre = view.findViewById(R.id.tvNombre);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        TextView tvRol = view.findViewById(R.id.tvRol);
        LinearLayout menu = view.findViewById(R.id.menuContainer);

        String nombre = session.getNombre();
        tvInicial.setText(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
        ImageView ivFoto = view.findViewById(R.id.ivFoto);
        Bitmap foto = FotoUtil.cargar(requireContext());
        if (foto != null) {
            ivFoto.setImageBitmap(foto);
            ivFoto.setVisibility(View.VISIBLE);
            tvInicial.setVisibility(View.GONE);
        }
        tvNombre.setText(nombre);
        tvEmail.setText(session.getEmail());
        tvRol.setText(session.esAdmin() ? "Administrador" : "Técnico");

        // Se mantiene actualizado el número de cambios sin enviar, para avisar al cerrar sesión
        new PendientesLiveData(requireContext()).observe(getViewLifecycleOwner(),
                n -> pendientes = n == null ? 0 : n);

        agregarOpcion(menu, R.drawable.ic_person, "Editar Perfil",
                v -> abrir(new EditarPerfilFragment()));
        agregarOpcion(menu, R.drawable.ic_security, "Seguridad",
                v -> abrir(new SeguridadFragment()));
        agregarOpcion(menu, R.drawable.ic_settings, "Configuración",
                v -> abrir(new ConfiguracionFragment()));
        if (session.esAdmin()) {
            agregarOpcion(menu, R.drawable.ic_location, "Ubicaciones",
                    v -> abrir(new UbicacionesFragment()));
        }
        agregarOpcion(menu, R.drawable.ic_logout, "Cerrar Sesión",
                v -> confirmarCierreDeSesion());
    }

    private void agregarOpcion(LinearLayout contenedor, int icono, String titulo,
                               View.OnClickListener accion) {
        View fila = getLayoutInflater().inflate(R.layout.item_menu_perfil, contenedor, false);
        ((ImageView) fila.findViewById(R.id.ivIcono)).setImageResource(icono);
        ((TextView) fila.findViewById(R.id.tvTitulo)).setText(titulo);
        fila.setOnClickListener(accion);
        contenedor.addView(fila);
    }

    private void abrir(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }


    private void confirmarCierreDeSesion() {
        Context app = requireContext().getApplicationContext();
        MaterialAlertDialogBuilder dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión", (d, w) ->
                        SyncManager.get(app).cerrarSesion(() -> {
                            Intent i = new Intent(app, LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            app.startActivity(i);
                        }));

        if (pendientes > 0) {
            dialogo.setMessage("Tienes " + pendientes + " cambio(s) sin sincronizar. "
                            + "Si cierras sesión, se perderán.")
                    .setNeutralButton("Sincronizar ahora", (d, w) ->
                            SyncManager.get(app).sincronizar((ok, mensaje) ->
                                    Toast.makeText(app,
                                            ok ? "Sincronización completada" : mensaje,
                                            Toast.LENGTH_LONG).show()));
        } else {
            dialogo.setMessage("¿Seguro que quieres cerrar sesión?");
        }
        dialogo.show();
    }
}