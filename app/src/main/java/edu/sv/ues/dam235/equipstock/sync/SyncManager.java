package edu.sv.ues.dam235.equipstock.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.repositories.CategoriaRepository;
import edu.sv.ues.dam235.equipstock.repositories.EquipoRepository;
import edu.sv.ues.dam235.equipstock.repositories.MantenimientoRepository;
import edu.sv.ues.dam235.equipstock.repositories.TecnicoRepository;
import edu.sv.ues.dam235.equipstock.repositories.UbicacionRepository;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncManager {

    private static final String PREFS = "equipstock_sync";
    private static final String KEY_ULTIMA = "ultima_sync";

    private static SyncManager instance;

    public static synchronized SyncManager get(Context context) {
        if (instance == null) {
            instance = new SyncManager(context.getApplicationContext());
            instance.sincronizarAlVolverInternet(context.getApplicationContext());
        }
        return instance;
    }

    private final SessionManager session;
    private final SharedPreferences prefs;

    private final Context appContext;

    private final List<Sincronizable> modulos = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private static final String MSG_SIN_CONEXION =
            "Sin conexión. Los cambios se enviarán cuando haya internet.";

    private SyncManager(Context context) {
        appContext = context;

        session = new SessionManager(context);
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // ORDEN IMPORTANTE: primero lo que otros módulos referencian
        modulos.add(CategoriaRepository.get(context));
        modulos.add(UbicacionRepository.get(context));
        modulos.add(EquipoRepository.get(context));
        modulos.add(TecnicoRepository.get(context));
        modulos.add(MantenimientoRepository.get(context));
    }

    private void sincronizarAlVolverInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                sincronizar(null);
            }
        });
    }

    public void sincronizar(Resultado callback) {
        executor.execute(() -> {
            if (!session.haySesion()) {
                notificar(callback, null);
                return;
            }
            String primerError = null;
            try {
                for (Sincronizable m : modulos) {
                    String error = m.sincronizarAhora();
                    if (error != null && primerError == null) primerError = error;
                }
            } catch (IOException e) {
                primerError = MSG_SIN_CONEXION;
            }
            if (primerError == null) {
                prefs.edit().putLong(KEY_ULTIMA, System.currentTimeMillis()).apply();
            }
            notificar(callback, primerError);
        });
    }

    /** Milisegundos de la última sincronización completa, o 0 si nunca hubo. */
    public long getUltimaSincronizacion() {
        return prefs.getLong(KEY_ULTIMA, 0);
    }

    private void notificar(Resultado callback, String error) {
        main.post(() -> {
            if (callback != null) {
                callback.alTerminar(error == null, error);
            } else if (error != null && !MSG_SIN_CONEXION.equals(error)) {
                // sincronización automática: nadie esperaba el resultado, se avisa igual
                Toast.makeText(appContext, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Cierra la sesión y borra los datos locales,
     * tras esperar a que termine cualquier sincronización en curso.
     */
    public void cerrarSesion(Runnable alTerminar) {
        executor.execute(() -> {
            session.cerrarSesion();
            AppDatabase.get(appContext).clearAllTables();
            main.post(alTerminar);
        });
    }

}