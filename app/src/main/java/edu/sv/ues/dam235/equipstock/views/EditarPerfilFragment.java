package edu.sv.ues.dam235.equipstock.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.PerfilApi;
import edu.sv.ues.dam235.equipstock.models.PerfilRequest;
import edu.sv.ues.dam235.equipstock.models.Usuario;
import edu.sv.ues.dam235.equipstock.utils.ApiErrores;
import edu.sv.ues.dam235.equipstock.utils.FotoUtil;
import edu.sv.ues.dam235.equipstock.utils.SessionManager;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilFragment extends Fragment {

    private static final String SIN_CONEXION = "Necesitas conexión a internet para esta operación.";
    private static final String SESION_EXPIRADA = "Tu sesión expiró. Cierra sesión y vuelve a entrar.";

    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private Uri uriCaptura;

    private Context app;
    private SessionManager session;
    private PerfilApi api;
    private ImageView ivFoto;
    private TextView tvInicial;
    private ImageButton btnCamara;
    private MaterialButton btnGuardar;
    private ProgressBar progress;

    // El selector de fotos del sistema no necesita permisos de almacenamiento
    private final ActivityResultLauncher<PickVisualMediaRequest> galeria =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) procesarYSubir(uri);
            });

    private final ActivityResultLauncher<Uri> camara =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (Boolean.TRUE.equals(exito) && uriCaptura != null) procesarYSubir(uriCaptura);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        app = requireContext().getApplicationContext();
        session = new SessionManager(app);
        api = ApiClient.get(app).create(PerfilApi.class);

        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        tvInicial = view.findViewById(R.id.tvInicial);
        ivFoto = view.findViewById(R.id.ivFoto);
        btnCamara = view.findViewById(R.id.btnCamara);
        TextView tvNombreCabecera = view.findViewById(R.id.tvNombreCabecera);
        TextInputLayout tilNombre = view.findViewById(R.id.tilNombre);
        TextInputLayout tilTelefono = view.findViewById(R.id.tilTelefono);
        TextInputLayout tilCorreo = view.findViewById(R.id.tilCorreo);
        TextInputEditText etNombre = view.findViewById(R.id.etNombre);
        TextInputEditText etTelefono = view.findViewById(R.id.etTelefono);
        TextInputEditText etCorreo = view.findViewById(R.id.etCorreo);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        progress = view.findViewById(R.id.progress);

        String nombreActual = session.getNombre();
        tvInicial.setText(nombreActual.isEmpty() ? "?" : nombreActual.substring(0, 1).toUpperCase());
        tvNombreCabecera.setText(nombreActual);
        mostrarAvatar();

        if (savedInstanceState == null) {
            etNombre.setText(nombreActual);
            etTelefono.setText(session.getTelefono());
        }
        // El correo es la identidad de la cuenta: se muestra, pero no se edita
        etCorreo.setText(session.getEmail());
        tilCorreo.setEnabled(false);
        etCorreo.setEnabled(false);

        btnVolver.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnCamara.setOnClickListener(v -> mostrarOpcionesFoto());
        ivFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        tvInicial.setOnClickListener(v -> mostrarOpcionesFoto());

        btnGuardar.setOnClickListener(v -> {
            tilNombre.setError(null);
            tilTelefono.setError(null);

            String nombre = texto(etNombre).trim();
            String telefono = texto(etTelefono).trim();

            boolean valido = true;
            if (nombre.length() < 3) {
                tilNombre.setError("Ingresa tu nombre");
                valido = false;
            }
            if (!telefono.isEmpty()) {
                int digitos = telefono.replaceAll("\\D", "").length();
                if (digitos < 8 || digitos > 15) {
                    tilTelefono.setError("Ingresa un teléfono válido");
                    valido = false;
                }
            }
            if (!valido) return;

            setCargando(true);
            api.actualizar(new PerfilRequest(nombre, telefono.isEmpty() ? null : telefono))
                    .enqueue(new Callback<Usuario>() {
                        @Override
                        public void onResponse(Call<Usuario> call, Response<Usuario> r) {
                            setCargando(false);
                            if (r.isSuccessful() && r.body() != null) {
                                session.actualizarPerfil(r.body().getNombre(),
                                        r.body().getTelefono());
                                Toast.makeText(app, "Perfil actualizado",
                                        Toast.LENGTH_LONG).show();
                                if (isAdded()) getParentFragmentManager().popBackStack();
                            } else if (r.code() == 401 || r.code() == 403) {
                                Toast.makeText(app, SESION_EXPIRADA, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(app, "No se pudo guardar (" + r.code() + ")",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Usuario> call, Throwable t) {
                            setCargando(false);
                            Toast.makeText(app, SIN_CONEXION, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ejecutor.shutdown();
    }

    // ---------- Foto de perfil ----------

    private void mostrarOpcionesFoto() {
        boolean hayFoto = FotoUtil.existe(app);
        String[] opciones = hayFoto
                ? new String[]{"Tomar foto", "Elegir de la galería", "Eliminar foto"}
                : new String[]{"Tomar foto", "Elegir de la galería"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.foto_cambiar_titulo)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        tomarFoto();
                    } else if (which == 1) {
                        galeria.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else {
                        confirmarEliminarFoto();
                    }
                })
                .show();
    }

    private void tomarFoto() {
        File carpeta = new File(app.getCacheDir(), "fotos");
        carpeta.mkdirs();
        File archivo = new File(carpeta, "captura.jpg");
        uriCaptura = FileProvider.getUriForFile(app, app.getPackageName() + ".fileprovider", archivo);
        camara.launch(uriCaptura);
    }

    private void procesarYSubir(Uri uri) {
        setCargando(true);
        ejecutor.execute(() -> {
            byte[] bytes;
            try {
                bytes = FotoUtil.procesar(app, uri);
            } catch (Exception e) {
                main.post(() -> {
                    setCargando(false);
                    Toast.makeText(app, "No se pudo leer la imagen", Toast.LENGTH_LONG).show();
                });
                return;
            }
            main.post(() -> subir(bytes));
        });
    }

    private void subir(byte[] bytes) {
        RequestBody cuerpo = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
        api.subirFoto(cuerpo).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> r) {
                setCargando(false);
                if (r.isSuccessful() && r.body() != null) {
                    Long version = r.body().getFotoActualizada();
                    long v = version == null ? 0 : version;
                    try {
                        FotoUtil.guardar(app, bytes, v);
                        session.setFotoActualizada(v);
                    } catch (IOException e) {
                        Toast.makeText(app, "No se pudo guardar la foto en el teléfono",
                                Toast.LENGTH_LONG).show();
                    }
                    mostrarAvatar();
                    Toast.makeText(app, "Foto actualizada", Toast.LENGTH_SHORT).show();
                } else if (r.code() == 400) {
                    String msg = ApiErrores.mensaje(r);
                    Toast.makeText(app, msg != null ? msg : "La imagen no es válida",
                            Toast.LENGTH_LONG).show();
                } else if (r.code() == 401 || r.code() == 403) {
                    Toast.makeText(app, SESION_EXPIRADA, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(app, "No se pudo subir la foto (" + r.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                setCargando(false);
                Toast.makeText(app, SIN_CONEXION, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarEliminarFoto() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Eliminar tu foto de perfil?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, w) -> eliminarFoto())
                .show();
    }

    private void eliminarFoto() {
        setCargando(true);
        api.eliminarFoto().enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> r) {
                setCargando(false);
                if (r.isSuccessful()) {
                    FotoUtil.borrar(app);
                    session.setFotoActualizada(0);
                    mostrarAvatar();
                    Toast.makeText(app, "Foto eliminada", Toast.LENGTH_SHORT).show();
                } else if (r.code() == 401 || r.code() == 403) {
                    Toast.makeText(app, SESION_EXPIRADA, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(app, "No se pudo eliminar (" + r.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                setCargando(false);
                Toast.makeText(app, SIN_CONEXION, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Muestra la foto guardada en el teléfono o, si no hay, la inicial del nombre. */
    private void mostrarAvatar() {
        if (getView() == null) return;
        Bitmap foto = FotoUtil.cargar(app);
        if (foto != null) {
            ivFoto.setImageBitmap(foto);
            ivFoto.setVisibility(View.VISIBLE);
            tvInicial.setVisibility(View.GONE);
        } else {
            ivFoto.setVisibility(View.GONE);
            tvInicial.setVisibility(View.VISIBLE);
        }
    }

    private void setCargando(boolean cargando) {
        if (getView() == null) return;
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
        btnCamara.setEnabled(!cargando);
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }
}