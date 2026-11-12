package edu.sv.ues.dam235.equipstock.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/** Sincroniza en segundo plano cada cierto tiempo, aunque la app esté cerrada. */
public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SyncManager.get(getApplicationContext()).sincronizarYEsperar();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}