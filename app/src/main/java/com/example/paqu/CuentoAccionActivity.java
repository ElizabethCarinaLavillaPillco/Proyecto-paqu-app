package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

public class CuentoAccionActivity extends BaseActivity {

    private CardView filterHistorias, filterInfantil, filterLeyendas;
    private CardView cardCuento1, cardCuento2, cardCuento3;
    private TextView textFilterHistorias, textFilterInfantil, textFilterLeyendas;

    private boolean historiaActiva = false;
    private boolean infantilActiva = false;
    private boolean leyendaActiva = false;
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
        filtrarCuentos();
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
            historiaActiva = !historiaActiva;
            toggleFilter(filterHistorias, textFilterHistorias, historiaActiva);
            filtrarCuentos();
        });

        filterInfantil.setOnClickListener(v -> {
            infantilActiva = !infantilActiva;
            toggleFilter(filterInfantil, textFilterInfantil, infantilActiva);
            filtrarCuentos();
        });

        filterLeyendas.setOnClickListener(v -> {
            leyendaActiva = !leyendaActiva;
            toggleFilter(filterLeyendas, textFilterLeyendas, leyendaActiva);
            filtrarCuentos();
        });
    }

    private void toggleFilter(CardView cardView, TextView textView, boolean activo) {

        if (activo) {
            cardView.setCardBackgroundColor(
                    getResources().getColor(R.color.filter_selected));
            textView.setTextColor(
                    getResources().getColor(R.color.black));
        } else {
            cardView.setCardBackgroundColor(
                    getResources().getColor(R.color.filter_unselected));
            textView.setTextColor(
                    getResources().getColor(R.color.white));
        }
    }

    private void filtrarCuentos() {

        boolean historiasSeleccionado = historiaActiva;
        boolean infantilSeleccionado = infantilActiva;
        boolean leyendasSeleccionado = leyendaActiva;

        // Si no hay filtros activos, mostrar todo
        if (!historiasSeleccionado &&
                !infantilSeleccionado &&
                !leyendasSeleccionado) {

            cardCuento1.setVisibility(android.view.View.VISIBLE);
            cardCuento2.setVisibility(android.view.View.VISIBLE);
            cardCuento3.setVisibility(android.view.View.VISIBLE);
            return;
        }

        // cardCuento1 = Leyenda
        cardCuento1.setVisibility(
                leyendasSeleccionado
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );

        // cardCuento2 = Infantil
        cardCuento2.setVisibility(
                infantilSeleccionado
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );

        // cardCuento3 = Historia
        cardCuento3.setVisibility(
                historiasSeleccionado
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );
    }

    private void setupCuentoCards() {
        cardCuento1.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(CuentoAccionActivity.this, VistaCuentoActivity.class);
                intent.putExtra("cuento_id", 1);
                intent.putExtra("titulo", "La Leyenda del Zorro");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error al abrir el cuento", Toast.LENGTH_SHORT).show();
            }
        });

        cardCuento2.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(CuentoAccionActivity.this, VistaCuentoActivity.class);
                intent.putExtra("cuento_id", 2);
                intent.putExtra("titulo", "Aventuras en los Andes");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error al abrir el cuento", Toast.LENGTH_SHORT).show();
            }
        });

        cardCuento3.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(CuentoAccionActivity.this, VistaCuentoActivity.class);
                intent.putExtra("cuento_id", 3);
                intent.putExtra("titulo", "El Origen de las Montañas");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error al abrir el cuento", Toast.LENGTH_SHORT).show();
            }
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
        return R.id.nav_Minijuegos;
    }
}