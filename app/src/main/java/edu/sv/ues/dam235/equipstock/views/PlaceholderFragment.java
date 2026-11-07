package edu.sv.ues.dam235.equipstock.views;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import edu.sv.ues.dam235.equipstock.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITULO = "titulo";

    public static PlaceholderFragment nueva(String titulo) {
        PlaceholderFragment f = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO, titulo);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setText(getArguments() != null ? getArguments().getString(ARG_TITULO) : "");
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(24);
        tv.setTextColor(getResources().getColor(R.color.text_on_background, null));
        return tv;
    }
}