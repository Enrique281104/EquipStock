package edu.sv.ues.dam235.equipstock.sync;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public final class SyncScheduler {

    private static final String NOMBRE = "sync_periodica";

    private SyncScheduler() { }

    public static void programar(Context context) {
        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest pedido = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(restricciones)
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(NOMBRE, ExistingPeriodicWorkPolicy.KEEP, pedido);
    }

    public static void cancelar(Context context) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(NOMBRE);
    }
}