package com.example.paqu;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class VistaCuentoActivity extends AppCompatActivity {

    private TextView tvTituloCuento, tvContenidoEspanol, tvContenidoQuechua, tvIdiomaActual;
    private CardView btnIdioma;
    private boolean esEspanol = true;

    // Datos de ejemplo del cuento
    private String[] titulos = {"La Leyenda del Zorro", "Atuqpa Willan"};
    private String[] contenidosEspanol = {
            "Había una vez un zorro muy astuto que vivía en los Andes. Con su inteligencia ayudaba a los animales del bosque a resolver problemas y superar desafíos. Todos lo admiraban por su sabiduría.",
            "En las altas montañas, el zorro enseñaba lecciones valiosas sobre la vida y la naturaleza."
    };

    private String[] contenidosQuechua = {
            "Ñawpaq pachapi astutu atuq kashan, Andespi tiyashan. Paypa yachayninwan sach'api uywakunata yanapan kashan sasachakuyninkunata allichanapaq. Llapallan payta yupaychanku yachayninrayku.",
            "Hanaq urqukunapi, atuq yachachiran valiosas lecciones kawsaymanta pachamantamanta."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_cuento);

        setupUI();
        cargarCuento();
        aplicarResaltadoIdioma(); // Aplicar resaltado inicial
    }

    private void setupUI() {
        tvTituloCuento = findViewById(R.id.tvTituloCuento);
        tvContenidoEspanol = findViewById(R.id.tvContenidoEspanol);
        tvContenidoQuechua = findViewById(R.id.tvContenidoQuechua);
        tvIdiomaActual = findViewById(R.id.tvIdiomaActual);
        btnIdioma = findViewById(R.id.btnIdioma);

        btnIdioma.setOnClickListener(v -> cambiarIdioma());
    }

    private void cargarCuento() {
        tvTituloCuento.setText(titulos[0]);
        tvContenidoEspanol.setText(contenidosEspanol[0]);
        tvContenidoQuechua.setText(contenidosQuechua[0]);
    }

    private void cambiarIdioma() {
        esEspanol = !esEspanol;
        aplicarResaltadoIdioma();
    }

    private void aplicarResaltadoIdioma() {
        if (esEspanol) {
            // Español resaltado
            tvIdiomaActual.setText("ES");
            tvTituloCuento.setText(titulos[0]);

            // Español grande y blanco brillante
            tvContenidoEspanol.setTextSize(16);
            tvContenidoEspanol.setTextColor(getResources().getColor(android.R.color.white));
            tvContenidoEspanol.setAlpha(1.0f);
            tvContenidoEspanol.setTypeface(tvContenidoEspanol.getTypeface(), android.graphics.Typeface.BOLD);

            // Quechua pequeño y gris
            tvContenidoQuechua.setTextSize(12);
            tvContenidoQuechua.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvContenidoQuechua.setAlpha(0.8f);
            tvContenidoQuechua.setTypeface(tvContenidoQuechua.getTypeface(), android.graphics.Typeface.NORMAL);

        } else {
            // Quechua resaltado
            tvIdiomaActual.setText("QU");
            tvTituloCuento.setText(titulos[1]);

            // Quechua grande y blanco brillante
            tvContenidoQuechua.setTextSize(16);
            tvContenidoQuechua.setTextColor(getResources().getColor(android.R.color.white));
            tvContenidoQuechua.setAlpha(1.0f);
            tvContenidoQuechua.setTypeface(tvContenidoQuechua.getTypeface(), android.graphics.Typeface.BOLD);

            // Español pequeño y gris
            tvContenidoEspanol.setTextSize(12);
            tvContenidoEspanol.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvContenidoEspanol.setAlpha(0.8f);
            tvContenidoEspanol.setTypeface(tvContenidoEspanol.getTypeface(), android.graphics.Typeface.NORMAL);
        }
    }
    public void volverAtras(View view) {
        finish();
    }
}