package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.paqu.activities.TimeAttackActivity;

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
        if (texto.contains("Memoria") || texto.contains("Contra el Reloj")) return 20f;
        if (texto.contains("parejas") || texto.contains("Responde antes")) return 14f;
        if (texto.contains("Próximamente") || texto.contains("Disponible")) return 12f;
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
        //CardView cardContrarreloj = findViewById(R.id.cardContrarreloj);
        CardView cardCuentos = findViewById(R.id.cardCuentos);

        // Juego de Memoria
        cardMemoria.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.paqu.activities.MemoriaActivity.class);
            startActivity(intent);
        });

        // Juego Contra el Reloj
        //cardContrarreloj.setOnClickListener(v -> {
        //    Intent intent = new Intent(this, com.example.paqu.activities.TimeAttackActivity.class);
        //     startActivity(intent);
        // });

        // Cuentos Interactivos
        cardCuentos.setOnClickListener(v -> {
            Intent intent = new Intent(this,
                    CuentoAccionActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_Minijuegos;
    }
}