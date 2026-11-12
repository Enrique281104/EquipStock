package edu.sv.ues.dam235.equipstock.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FotoUtil {

    private static final String PREFS = "equipstock_foto";
    private static final String KEY_VERSION = "version_local";
    private static final int LADO_MAX = 512;
    private static final int CALIDAD = 85;

    private FotoUtil() { }

    private static File archivo(Context c) {
        return new File(c.getApplicationContext().getFilesDir(), "foto_perfil.jpg");
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean existe(Context c) {
        return archivo(c).exists();
    }

    /** Versión (fecha de cambio) de la foto guardada en el teléfono; 0 si no hay. */
    public static long versionLocal(Context c) {
        return prefs(c).getLong(KEY_VERSION, 0);
    }

    public static Bitmap cargar(Context c) {
        File f = archivo(c);
        return f.exists() ? BitmapFactory.decodeFile(f.getAbsolutePath()) : null;
    }

    public static void guardar(Context c, byte[] datos, long version) throws IOException {
        try (FileOutputStream out = new FileOutputStream(archivo(c))) {
            out.write(datos);
        }
        prefs(c).edit().putLong(KEY_VERSION, version).apply();
    }

    public static void borrar(Context c) {
        archivo(c).delete();
        prefs(c).edit().clear().apply();
    }

    /**
     * Recorta al centro en cuadrado, reduce a 512 px y comprime a JPEG.
     * Debe llamarse fuera del hilo principal.
     */
    public static byte[] procesar(Context c, Uri uri) throws IOException {
        Bitmap origen = decodificar(c, uri);
        if (origen == null) throw new IOException("No se pudo leer la imagen");

        int lado = Math.min(origen.getWidth(), origen.getHeight());
        Bitmap cuadrado = Bitmap.createBitmap(origen,
                (origen.getWidth() - lado) / 2, (origen.getHeight() - lado) / 2, lado, lado);
        Bitmap resultado = lado > LADO_MAX
                ? Bitmap.createScaledBitmap(cuadrado, LADO_MAX, LADO_MAX, true) : cuadrado;

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        resultado.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida);
        return salida.toByteArray();
    }

    private static Bitmap decodificar(Context c, Uri uri) throws IOException {
        ContentResolver cr = c.getContentResolver();

        if (Build.VERSION.SDK_INT >= 28) {
            // ImageDecoder respeta la orientación de la foto
            ImageDecoder.Source fuente = ImageDecoder.createSource(cr, uri);
            return ImageDecoder.decodeBitmap(fuente, (decoder, info, src) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                int w = info.getSize().getWidth();
                int h = info.getSize().getHeight();
                int menor = Math.min(w, h);
                if (menor > 2 * LADO_MAX) {
                    float escala = (2f * LADO_MAX) / menor;
                    decoder.setTargetSize(Math.round(w * escala), Math.round(h * escala));
                }
            });
        }

        // Android 8.x: sin corrección de orientación
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        try (InputStream in = cr.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, limites);
        }
        int menor = Math.min(limites.outWidth, limites.outHeight);
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = Math.max(1, menor / (2 * LADO_MAX));
        try (InputStream in = cr.openInputStream(uri)) {
            return BitmapFactory.decodeStream(in, null, opciones);
        }
    }
}