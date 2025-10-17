package com.example.paqu;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class diccionarioActivity extends BaseActivity {
    private TextToSpeech tts;
    private Button btnCerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diccionario);

        // NUEVO: Aplicar fuentes automáticamente
        aplicarFuentesAutomaticas();

        // Inicializar btnCerrar antes de usarlo
        btnCerrar = findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> {
            startActivity(new Intent(diccionarioActivity.this, homeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });

        // Inicializar TextToSpeech
        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(new Locale("es", "ES"));
            }
        });

        // Configurar botones y frases
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);

        // Reproducir sonidos
        button2.setOnClickListener(v -> playSound(R.raw.voz1));   // voz1.mp3
        button3.setOnClickListener(v -> playSound(R.raw.voz2));   // voz2.mp3
        button4.setOnClickListener(v -> playSound(R.raw.voz3));   // ejemplo: voz3.mp3
        button5.setOnClickListener(v -> playSound(R.raw.voz4));   // ejemplo: voz4.mp3
        button6.setOnClickListener(v -> playSound(R.raw.voz5));   // ejemplo: voz5.mp3
        button7.setOnClickListener(v -> playSound(R.raw.voz6));   // ejemplo: voz6.mp3
    }

    // NUEVO: Método para aplicar fuentes a TODOS los TextView
    private void aplicarFuentesAutomaticas() {
        // TextView principales
        aplicarFuente(R.id.textView14, 20f);  // "¡ Mejora tu pronunciación del Quechua !"
        aplicarFuente(R.id.textView15, 16f);  // "dxsexrdctfvghbjnkml,ñ"
        aplicarFuente(R.id.textView16, 20f);  // "Frases cotidianas - Kawsaypi rimanakuna"

        // También puedes aplicar a los botones si quieres
        aplicarFuenteBotones();
    }

    // NUEVO: Método para aplicar fuentes a los textos de los botones
    private void aplicarFuenteBotones() {
        int[] botonesIds = {
                R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7
        };

        for (int botonId : botonesIds) {
            Button boton = findViewById(botonId);
            if (boton != null) {
                // Aplicar tamaño de fuente al texto del botón
                configuracionActivity.aplicarTamanioFuente(boton, 14f);
            }
        }
    }

    // NUEVO: Método helper (igual que en homeActivity)
    private void aplicarFuente(int textViewId, float tamanioBase) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            configuracionActivity.aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    private void playSound(int soundResId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);
        if (mediaPlayer != null) {
            mediaPlayer.start();

            // Opcional: liberar recursos después de terminar
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // NUEVO: Re-aplicar fuentes por si hubo cambios
        aplicarFuentesAutomaticas();
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_dictionary;
    }
}