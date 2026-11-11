package edu.sv.ues.dam235.equipstock.views.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import edu.sv.ues.dam235.equipstock.R;

public class DonutView extends View {

    private final Paint arco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numero = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint etiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int colorCompletado;
    private final int colorProceso;
    private final int colorVencido;
    private final int colorVacio;
    private final String textoTotal;
    private int completados;
    private int enProceso;
    private int vencidos;

    public DonutView(Context context) {
        this(context, null);
    }

    public DonutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        colorCompletado = ContextCompat.getColor(context, R.color.dash_completado);
        colorProceso = ContextCompat.getColor(context, R.color.dash_proceso);
        colorVencido = ContextCompat.getColor(context, R.color.dash_vencido);
        colorVacio = ContextCompat.getColor(context, R.color.field_background);
        textoTotal = context.getString(R.string.dashboard_total);

        arco.setStyle(Paint.Style.STROKE);
        int textoColor = ContextCompat.getColor(context, R.color.text_primary);

        numero.setColor(textoColor);
        numero.setTextAlign(Paint.Align.CENTER);
        numero.setFakeBoldText(true);
        numero.setTextSize(sp(28));

        etiqueta.setColor(textoColor);
        etiqueta.setAlpha(180);
        etiqueta.setTextAlign(Paint.Align.CENTER);
        etiqueta.setTextSize(sp(11));
    }

    public void setValores(int completados, int enProceso, int vencidos) {
        this.completados = completados;
        this.enProceso = enProceso;
        this.vencidos = vencidos;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float grosor = dp(18);
        arco.setStrokeWidth(grosor);
        float lado = Math.min(getWidth(), getHeight());
        float izq = (getWidth() - lado) / 2f + grosor / 2f;
        float arriba = (getHeight() - lado) / 2f + grosor / 2f;
        rect.set(izq, arriba, izq + lado - grosor, arriba + lado - grosor);

        int total = completados + enProceso + vencidos;
        if (total == 0) {
            arco.setColor(colorVacio);
            canvas.drawArc(rect, 0, 360, false, arco);
        } else {
            float inicio = -90f;
            inicio += trazar(canvas, inicio, completados, total, colorCompletado);
            inicio += trazar(canvas, inicio, enProceso, total, colorProceso);
            trazar(canvas, inicio, vencidos, total, colorVencido);
        }

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        canvas.drawText(String.valueOf(total), cx, cy + dp(4), numero);
        canvas.drawText(textoTotal, cx, cy + dp(22), etiqueta);
    }

    /** Dibuja un tramo y devuelve cuántos grados ocupó. */
    private float trazar(Canvas canvas, float inicio, int valor, int total, int color) {
        if (valor <= 0) return 0;
        float barrido = 360f * valor / total;
        arco.setColor(color);
        canvas.drawArc(rect, inicio, barrido, false, arco);
        return barrido;
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v,
                getResources().getDisplayMetrics());
    }
}