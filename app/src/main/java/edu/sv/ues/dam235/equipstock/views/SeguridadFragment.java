package edu.sv.ues.dam235.equipstock.views;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sv.ues.dam235.equipstock.R;
import edu.sv.ues.dam235.equipstock.api.ApiClient;
import edu.sv.ues.dam235.equipstock.api.PerfilApi;
import edu.sv.ues.dam235.equipstock.models.PasswordRequest;
import edu.sv.ues.dam235.equipstock.utils.ApiErrores;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeguridadFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seguridad, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context app = requireContext().getApplicationContext();
        PerfilApi api = ApiClient.get(app).create(PerfilApi.class);

        ImageButton btnVolver = view.findViewById(R.id.btnVolver);
        TextInputLayout tilActual = view.findViewById(R.id.tilActual);
        TextInputLayout tilNueva = view.findViewById(R.id.tilNueva);
        TextInputLayout tilConfirmar = view.findViewById(R.id.tilConfirmar);
        TextInputEditText etActual = view.findViewById(R.id.etActual);
        TextInputEditText etNueva = view.findViewById(R.id.etNueva);
        TextInputEditText etConfirmar = view.findViewById(R.id.etConfirmar);
        MaterialButton btnActualizar = view.findViewById(R.id.btnActualizar);
        ProgressBar progress = view.findViewById(R.id.progress);

        btnVolver.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnActualizar.setOnClickListener(v -> {
            tilActual.setError(null);
            tilNueva.setError(null);
            tilConfirmar.setError(null);

            String actual = texto(etActual);
            String nueva = texto(etNueva);
            String confirmar = texto(etConfirmar);

            boolean valido = true;
            if (actual.isEmpty()) {
                tilActual.setError("Ingresa tu contraseña actual");
                valido = false;
            }
            if (nueva.length() < 8) {
                tilNueva.setError("Mínimo 8 caracteres");
                valido = false;
            } else if (nueva.equals(actual)) {
                tilNueva.setError("Debe ser distinta de la actual");
                valido = false;
            }
            if (!nueva.equals(confirmar)) {
                tilConfirmar.setError("Las contraseñas no coinciden");
                valido = false;
            }
            if (!valido) return;

            btnActualizar.setEnabled(false);
            progress.setVisibility(View.VISIBLE);

            api.cambiarPassword(new PasswordRequest(actual, nueva)).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> r) {
                    if (getView() != null) {
                        btnActualizar.setEnabled(true);
                        progress.setVisibility(View.GONE);
                    }
                    if (r.isSuccessful()) {
                        Toast.makeText(app, "Contraseña actualizada", Toast.LENGTH_LONG).show();
                        if (isAdded()) getParentFragmentManager().popBackStack();
                    } else if (r.code() == 400) {
                        String msg = ApiErrores.mensaje(r);
                        if (msg != null && msg.contains("incorrecta") && getView() != null) {
                            tilActual.setError(msg);
                        } else {
                            Toast.makeText(app, msg != null ? msg : "Datos inválidos",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else if (r.code() == 401 || r.code() == 403) {
                        Toast.makeText(app,
                                "Tu sesión expiró. Cierra sesión y vuelve a entrar.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(app, "Error del servidor (" + r.code() + ")",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    if (getView() != null) {
                        btnActualizar.setEnabled(true);
                        progress.setVisibility(View.GONE);
                    }
                    Toast.makeText(app, "Necesitas conexión a internet para esta operación.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }
}