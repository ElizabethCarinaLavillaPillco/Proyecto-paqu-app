package com.example.paqu;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

public class CuentoAccionActivity extends BaseActivity {

    private CardView filterHistorias, filterInfantil, filterLeyendas;
    private CardView cardCuento1, cardCuento2, cardCuento3;
    private TextView textFilterHistorias, textFilterInfantil, textFilterLeyendas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuento_accion);

        // Aplicar fuentes automáticamente
        aplicarFuentesAutomaticas();

        // Configurar elementos de la UI
        setupUIElements();
        setupFilters();
        setupCuentoCards();
    }

    private void setupUIElements() {
        // Inicializar filtros
        filterHistorias = findViewById(R.id.filterHistorias);
        filterInfantil = findViewById(R.id.filterInfantil);
        filterLeyendas = findViewById(R.id.filterLeyendas);

        textFilterHistorias = findViewById(R.id.textFilterHistorias);
        textFilterInfantil = findViewById(R.id.textFilterInfantil);
        textFilterLeyendas = findViewById(R.id.textFilterLeyendas);

        // Inicializar cards de cuentos
        cardCuento1 = findViewById(R.id.cardCuento1);
        cardCuento2 = findViewById(R.id.cardCuento2);
        cardCuento3 = findViewById(R.id.cardCuento3);
    }

    private void setupFilters() {
        // Filtro Historias
        filterHistorias.setOnClickListener(v -> {
            toggleFilter(filterHistorias, textFilterHistorias);
            filtrarCuentos();
        });

        // Filtro Infantil
        filterInfantil.setOnClickListener(v -> {
            toggleFilter(filterInfantil, textFilterInfantil);
            filtrarCuentos();
        });

        // Filtro Leyendas
        filterLeyendas.setOnClickListener(v -> {
            toggleFilter(filterLeyendas, textFilterLeyendas);
            filtrarCuentos();
        });
    }

    private void toggleFilter(CardView cardView, TextView textView) {
        boolean isSelected = cardView.getCardBackgroundColor().getDefaultColor() == getResources().getColor(R.color.filter_selected);

        if (isSelected) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.filter_unselected));
            textView.setTextColor(getResources().getColor(R.color.white));
        } else {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.filter_selected));
            textView.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void filtrarCuentos() {
        // Por ahora solo muestra mensaje, luego implementarás la lógica real
        Toast.makeText(this, "Filtrando cuentos...", Toast.LENGTH_SHORT).show();
    }

    private void setupCuentoCards() {
        cardCuento1.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo: La Leyenda del Zorro", Toast.LENGTH_SHORT).show();
        });

        cardCuento2.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo: Aventuras en los Andes", Toast.LENGTH_SHORT).show();
        });

        cardCuento3.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo: El Origen de las Montañas", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para aplicar fuentes (similar a MiniJuegosActivity)
    private void aplicarFuentesAutomaticas() {
        aplicarFuenteATodosLosTextView();
    }

    private void aplicarFuenteATodosLosTextView() {
        aplicarFuenteRecursiva(findViewById(android.R.id.content));
    }

    private void aplicarFuenteRecursiva(android.view.View view) {
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                aplicarFuenteRecursiva(viewGroup.getChildAt(i));
            }
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            String texto = textView.getText().toString();
            float tamanioBase = obtenerTamanioParaTexto(texto);
            // Asumiendo que tienes configuracionActivity disponible
            // configuracionActivity.aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    private float obtenerTamanioParaTexto(String texto) {
        if (texto.contains("📖 Cuentos en Acción")) return 32f;
        if (texto.contains("Explora historias")) return 16f;
        if (texto.contains("Filtrar por tipo") || texto.contains("Cuentos disponibles")) return 14f;
        if (texto.contains("La Leyenda del Zorro") || texto.contains("Aventuras en los Andes") || texto.contains("El Origen de las Montañas")) return 18f;
        if (texto.contains("Leyenda") || texto.contains("Infantil") || texto.contains("Historia")) return 10f;
        return 14f; // Tamaño por defecto
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarFuentesAutomaticas();
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_minijuegos;
    }
}