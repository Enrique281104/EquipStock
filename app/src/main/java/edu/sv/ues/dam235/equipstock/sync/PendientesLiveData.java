package edu.sv.ues.dam235.equipstock.sync;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;

/** Cantidad total de cambios locales que todavía no llegan al servidor. */
public class PendientesLiveData extends MediatorLiveData<Integer> {

    private final int[] valores = new int[4];

    public PendientesLiveData(Context context) {
        AppDatabase db = AppDatabase.get(context.getApplicationContext());
        agregar(db.categoriaDao().contarPendientes(), 0);
        agregar(db.ubicacionDao().contarPendientes(), 1);
        agregar(db.equipoDao().contarPendientes(), 2);
        agregar(db.mantenimientoDao().contarPendientes(), 3);
    }

    private void agregar(LiveData<Integer> fuente, int posicion) {
        addSource(fuente, n -> {
            valores[posicion] = n == null ? 0 : n;
            setValue(valores[0] + valores[1] + valores[2] + valores[3]);
        });
    }
}