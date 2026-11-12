package edu.sv.ues.dam235.equipstock.sync;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import edu.sv.ues.dam235.equipstock.MainActivity;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.database.AppDatabase;
import edu.sv.ues.dam235.equipstock.database.MantenimientoDao;
import edu.sv.ues.dam235.equipstock.utils.AjustesUtil;
import edu.sv.ues.dam235.equipstock.utils.FechaUtil;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;

public final class Notificador {

    private static final String CANAL = "mantenimientos";
    private static final int ID_NUEVOS = 1;
    private static final int ID_PENDIENTES = 2;

    private Notificador() { }

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL, "Mantenimientos", NotificationManager.IMPORTANCE_DEFAULT);
            canal.setDescription("Mantenimientos nuevos, vencidos y de hoy");
            context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
        }
    }

    /** Revisa la base local y muestra los avisos que correspondan. Corre tras cada sincronización. */
    public static void evaluar(Context context) {
        Context c = context.getApplicationContext();
        SessionManager session = new SessionManager(c);
        if (!session.haySesion()) return;

        MantenimientoDao dao = AppDatabase.get(c).mantenimientoDao();
        boolean admin = session.esAdmin();
        long tecnicoId = admin ? 0 : session.getUserId();

        // Los "nuevos" se marcan siempre, para que no se acumulen si los avisos están apagados
        int nuevos = admin ? 0 : dao.contarNuevosPara(tecnicoId);
        dao.limpiarNuevos();

        if (!AjustesUtil.notificacionesHabilitadas(c) || !AjustesUtil.permisoConcedido(c)) return;

        if (nuevos > 0) {
            mostrar(c, ID_NUEVOS, "Nuevo mantenimiento asignado",
                    nuevos == 1 ? "Tienes 1 mantenimiento nuevo."
                            : "Tienes " + nuevos + " mantenimientos nuevos.");
        }

        // Vencidos y de hoy: un aviso al día
        String hoy = FechaUtil.hoy();
        if (hoy.equals(AjustesUtil.fechaUltimoAviso(c))) return;

        int vencidos = dao.contarVencidosAhora(tecnicoId, hoy);
        int deHoy = dao.contarDeHoyAhora(tecnicoId, hoy);
        if (vencidos == 0 && deHoy == 0) return;

        String detalle = "";
        if (vencidos > 0) detalle += vencidos + (vencidos == 1 ? " vencido" : " vencidos");
        if (vencidos > 0 && deHoy > 0) detalle += " y ";
        if (deHoy > 0) detalle += deHoy + " para hoy";

        mostrar(c, ID_PENDIENTES, "Mantenimientos pendientes",
                (admin ? "Hay " : "Tienes ") + detalle + ".");
        AjustesUtil.guardarFechaAviso(c, hoy);
    }

    @SuppressLint("MissingPermission")   // el permiso se comprueba antes en evaluar()
    private static void mostrar(Context c, int id, String titulo, String texto) {
        Intent intent = new Intent(c, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent abrir = PendingIntent.getActivity(c, id, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CANAL)
                .setSmallIcon(R.drawable.ic_nav_mantenimiento)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setContentIntent(abrir)
                .setAutoCancel(true);
        NotificationManagerCompat.from(c).notify(id, b.build());
    }
}