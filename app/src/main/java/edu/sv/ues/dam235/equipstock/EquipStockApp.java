package edu.sv.ues.dam235.equipstock;

import android.app.Application;

import edu.sv.ues.dam235.equipstock.sync.Notificador;
import edu.sv.ues.dam235.equipstock.utils.TemaUtil;

public class EquipStockApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Notificador.crearCanal(this);
        TemaUtil.aplicar(this);
    }
}