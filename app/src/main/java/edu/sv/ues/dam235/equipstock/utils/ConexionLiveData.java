package edu.sv.ues.dam235.equipstock.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class ConexionLiveData extends LiveData<Boolean> {

    private final ConnectivityManager cm;
    private final ConnectivityManager.NetworkCallback callback =
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    postValue(false);
                }
            };

    public ConexionLiveData(Context context) {
        cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onActive() {
        Network red = cm.getActiveNetwork();
        NetworkCapabilities caps = red == null ? null : cm.getNetworkCapabilities(red);
        setValue(caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
        cm.registerDefaultNetworkCallback(callback);
    }

    @Override
    protected void onInactive() {
        cm.unregisterNetworkCallback(callback);
    }
}