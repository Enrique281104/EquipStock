package edu.sv.ues.dam235.equipstock.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.sync.PendientesLiveData;
import edu.sv.ues.dam235.equipstock.sync.SyncManager;
import edu.sv.ues.dam235.equipstock.utils.AjustesUtil;
import edu.sv.ues.dam235.equipstock.utils.ConexionLiveData;
import edu.sv.ues.dam235.equipstock.utils.FechaUtil;
import edu.sv.ues.dam235.equipstock.utils.TemaUtil;

public class ConfiguracionFragment extends Fragment {

    private MaterialSwitch swNotificaciones;

    private final ActivityResultLauncher<String> pedirPermiso =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                Context c = getContext();
                if (c == null) return;
                if (concedido) {
                    AjustesUtil.setNotificacionesHabilitadas(c, true);
                } else {
                    if (swNotificaciones != null) swNotificaciones.setChecked(false);
                    abrirAjustesDeNotificaciones();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = requireContext();
        Context app = ctx.getApplicationContext();

        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        MaterialSwitch swOscuro = view.findViewById(R.id.swOscuro);
        View punto = view.findViewById(R.id.puntoConexion);
        TextView tvConexion = view.findViewById(R.id.tvConexion);
        TextView tvUltimaSync = view.findViewById(R.id.tvUltimaSync);
        TextView tvPendientes = view.findViewById(R.id.tvPendientes);
        ImageButton btnSync = view.findViewById(R.id.btnSync);
        ProgressBar progress = view.findViewById(R.id.progressSync);

        btnVolver.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        swNotificaciones = view.findViewById(R.id.swNotificaciones);
        swNotificaciones.setChecked(AjustesUtil.notificacionesHabilitadas(ctx)
                && AjustesUtil.permisoConcedido(ctx));
        swNotificaciones.setOnCheckedChangeListener((b, marcado) -> {
            if (!marcado) {
                AjustesUtil.setNotificacionesHabilitadas(app, false);
            } else if (AjustesUtil.permisoConcedido(app)) {
                AjustesUtil.setNotificacionesHabilitadas(app, true);
            } else if (Build.VERSION.SDK_INT >= 33) {
                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                b.setChecked(false);
                abrirAjustesDeNotificaciones();
            }
        });

        // Modo oscuro: primero el estado actual, después el listener (para no dispararlo solo)
        swOscuro.setChecked(TemaUtil.esOscuroActivo(ctx));
        swOscuro.setOnCheckedChangeListener((b, marcado) -> TemaUtil.setOscuro(app, marcado));

        // Estado de la conexión
        new ConexionLiveData(ctx).observe(getViewLifecycleOwner(), enLinea -> {
            boolean ok = enLinea != null && enLinea;
            tvConexion.setText(ok ? R.string.config_en_linea : R.string.config_sin_conexion);
            punto.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx,
                    ok ? R.color.dash_completado : R.color.estado_mantenimiento_text)));
        });

        // Última sincronización y cambios pendientes
        SyncManager.get(app).ultimaSincronizacion().observe(getViewLifecycleOwner(), ms ->
                tvUltimaSync.setText(ms == null || ms == 0
                        ? getString(R.string.config_nunca) : FechaUtil.mostrarFechaHora(ms)));

        new PendientesLiveData(ctx).observe(getViewLifecycleOwner(), n ->
                tvPendientes.setText(String.valueOf(n == null ? 0 : n)));

        // Ejecutar sincronización
        btnSync.setOnClickListener(v -> {
            btnSync.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            SyncManager.get(app).sincronizar((ok, mensaje) -> {
                progress.setVisibility(View.GONE);
                btnSync.setVisibility(View.VISIBLE);
                Toast.makeText(app,
                        ok ? app.getString(R.string.config_sync_ok) : mensaje,
                        Toast.LENGTH_LONG).show();
            });
        });
    }

    private void abrirAjustesDeNotificaciones() {
        Context c = requireContext();
        Toast.makeText(c, R.string.config_notif_permiso, Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, c.getPackageName()));
    }

}