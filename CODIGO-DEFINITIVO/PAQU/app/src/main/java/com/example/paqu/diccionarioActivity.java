// diccionarioActivity.java
package com.example.paqu;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import java.util.Locale;

public class diccionarioActivity extends BaseActivity {
    private TextToSpeech tts;
    private Button btnCerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diccionario);

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


    private void playSound(int soundResId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);
        if (mediaPlayer != null) {
            mediaPlayer.start();

            // Opcional: liberar recursos después de terminar
            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
        }
    }



    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_dictionary;
    }
}