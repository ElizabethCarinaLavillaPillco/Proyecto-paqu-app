package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

public class MiniJuegosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_juegos);

        // NUEVO: Aplicar fuentes automáticamente
        aplicarFuentesAutomaticas();

        // Configurar los cards de minijuegos
        setupGameCards();
    }

    // NUEVO: Método para aplicar fuentes a TODOS los TextView
    private void aplicarFuentesAutomaticas() {
        // Buscar todos los TextView y aplicar según su contenido
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
            configuracionActivity.aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    private float obtenerTamanioParaTexto(String texto) {
        if (texto.contains("🎮 Minijuegos")) return 32f;
        if (texto.contains("Practica Quechua")) return 16f;
        if (texto.contains("Memoria") || texto.contains("Contra el Reloj") || texto.contains("Cuento en Acción")) return 20f; // ← AGREGAR "Cuento en Acción"
        if (texto.contains("parejas") || texto.contains("Responde antes") || texto.contains("Interactúa con historias")) return 14f; // ← AGREGAR "Interactúa con historias"
        if (texto.contains("Próximamente") || texto.contains("Disponible") || texto.contains("Disponible")) return 12f;
        if (texto.contains("💡")) return 12f;
        return 14f; // Tamaño por defecto
    }

    @Override
    protected void onResume() {
        super.onResume();
        // NUEVO: Re-aplicar fuentes por si hubo cambios
        aplicarFuentesAutomaticas();
    }

    private void setupGameCards() {
        CardView cardMemoria = findViewById(R.id.cardMemoria);
        CardView cardContrarreloj = findViewById(R.id.cardContrarreloj);
        CardView cardCuentoAccion = findViewById(R.id.cardCuentoAccion);

        // Juego de Memoria
        cardMemoria.setOnClickListener(v -> {
            Toast.makeText(this, "🎮 Sección en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Juego Contra el Reloj
        cardContrarreloj.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.paqu.activities.TimeAttackActivity.class);
            startActivity(intent);
        });
        // Cuento en Acción
        cardCuentoAccion.setOnClickListener(v -> {
            Intent intent = new Intent(MiniJuegosActivity.this, CuentoAccionActivity.class);
            startActivity(intent);
        });
    }


    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_Minijuegos;
    }
}