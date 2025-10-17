package com.example.paqu;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ejercicio1 extends AppCompatActivity {

    long tiempoInicio;
    LinearLayout optionTantaWawa, optionChusaqchakuy, optionWinaypaq, optionSamay;
    Button checkButton;
    String correctAnswer = "Allin p'unchay";
    String selectedAnswer = "";

    MediaPlayer audioPlayer, optionPlayer;
    long vidasActuales = 5;
    TextView vidasCount;

    // 🔊 Audio Manager
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio1);

        // 🔊 Inicializar AudioManager
        audioManager = AudioManager.getInstance(this);

        // 🔢 Número de ejercicio actual
        int ejercicio = 1;

        // 🟪 ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(ejercicio * 20);

        vidasCount = findViewById(R.id.livesCount);
        vidasActuales = getIntent().getLongExtra("vidas", 5);
        vidasCount.setText(String.valueOf(vidasActuales));

        if (vidasActuales <= 0) {
            Toast.makeText(this, "😢 Sin vidas. Intenta más tarde.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 🕓 Tiempo de inicio
        tiempoInicio = System.currentTimeMillis();

        // 🧩 Referencias
        optionTantaWawa = findViewById(R.id.opcion1);
        optionChusaqchakuy = findViewById(R.id.opcion2);
        optionWinaypaq = findViewById(R.id.opcion3);
        optionSamay = findViewById(R.id.opcion4);
        checkButton = findViewById(R.id.checkButton);

        // 🔊 Audio principal
        LinearLayout audioButton = findViewById(R.id.audioButton);
        audioPlayer = MediaPlayer.create(this, R.raw.voz_buenos_dias);
        audioButton.setOnClickListener(v -> {
            if (audioPlayer != null) {
                audioPlayer.start();
            }
            // Efecto de clic
            audioManager.reproducirClic();
        });

        // 🗣️ Opciones
        setupOption(optionTantaWawa, "Allin p'unchay", R.raw.voz_buenos_dias);
        setupOption(optionChusaqchakuy, "Allin tuta", R.raw.voz_buenas_noches);
        setupOption(optionWinaypaq, "Puñuy", R.raw.voz_dormir);
        setupOption(optionSamay, "Samay", R.raw.voz_samay);

        // ✅ Botón comprobar
        checkButton.setOnClickListener(v -> {
            if (selectedAnswer.isEmpty()) return;

            if (normalize(selectedAnswer).equals(normalize(correctAnswer))) {
                long duracion = System.currentTimeMillis() - tiempoInicio;
                // 🎉 Reproducir sonido de éxito
                audioManager.reproducirExito();
                showDialog("¡Bien hecho!", "¡Respuesta correcta! 🎉", duracion);
            } else {
                // ❌ Reproducir sonido de error
                audioManager.reproducirError();
                restarVida();
            }
        });
    }

    private void restarVida() {
        vidasActuales = Math.max(vidasActuales - 1, 0);
        vidasCount.setText(String.valueOf(vidasActuales));

        if (vidasActuales <= 0) {
            Toast.makeText(this, "💔 Sin vidas. Espera 1 hora.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "❌ Incorrecto. Te quedan " + vidasActuales + " vidas.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupOption(LinearLayout layout, String answer, int audioResId) {
        layout.setOnClickListener(v -> {
            clearSelections();
            layout.setSelected(true);
            selectedAnswer = answer;
            checkButton.setEnabled(true);
            checkButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));

            // Reproducir audio de la opción
            if (optionPlayer != null) {
                optionPlayer.release();
            }
            optionPlayer = MediaPlayer.create(this, audioResId);
            if (optionPlayer != null) {
                optionPlayer.start();
            }

            // Efecto de clic
            audioManager.reproducirClic();
        });
    }

    private void clearSelections() {
        optionTantaWawa.setSelected(false);
        optionChusaqchakuy.setSelected(false);
        optionWinaypaq.setSelected(false);
        optionSamay.setSelected(false);
    }

    private void showDialog(String title, String message, long duracion) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    Intent intent = new Intent(this, ejercicio2.class);
                    intent.putExtra("exp", 10);
                    intent.putExtra("tiempo", duracion);
                    intent.putExtra("nivel", 1);
                    intent.putExtra("ejercicio", 2);
                    intent.putExtra("vidas", vidasActuales);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String normalize(String s) {
        return s.trim()
                .toLowerCase()
                .replace("'", "'")
                .replace("'", "'")
                .replace("`", "'")
                .replace("´", "'")
                .replace("ʻ", "'");
    }

    @Override
    protected void onDestroy() {
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        if (optionPlayer != null) {
            optionPlayer.release();
            optionPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar audios al salir
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.pause();
        }
        if (optionPlayer != null && optionPlayer.isPlaying()) {
            optionPlayer.pause();
        }
    }
}