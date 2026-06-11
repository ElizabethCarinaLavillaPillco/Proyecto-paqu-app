package com.example.paqu;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ejercicio2_1 extends AppCompatActivity {

    long tiempoInicio;

    LinearLayout option1, option2, option3, option4;
    Button checkButton;
    ImageButton btnBackToMenu;

    String correctAnswer = "¿Cómo está?";
    String selectedAnswer = "";

    MediaPlayer audioPlayer, optionPlayer;

    long vidasActuales = 5;
    TextView vidasCount;

    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio21);

        audioManager = AudioManager.getInstance(this);

        int ejercicio = 1;

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(ejercicio * 50);

        vidasCount = findViewById(R.id.livesCount);

        vidasActuales = getIntent().getLongExtra("vidas", 5);
        vidasCount.setText(String.valueOf(vidasActuales));

        guardarVidas();

        if (vidasActuales <= 0) {
            Toast.makeText(this,
                    "😢 Sin vidas. Intenta más tarde.",
                    Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, homeActivity.class));
            finish();
            return;
        }

        tiempoInicio = System.currentTimeMillis();

        option1 = findViewById(R.id.opcion1);
        option2 = findViewById(R.id.opcion2);
        option3 = findViewById(R.id.opcion3);
        option4 = findViewById(R.id.opcion4);

        checkButton = findViewById(R.id.checkButton);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        btnBackToMenu.setOnClickListener(v -> mostrarDialogoSalir());

        LinearLayout audioButton = findViewById(R.id.audioButton);

        audioPlayer = MediaPlayer.create(this, R.raw.voz_imaynalla);

        audioButton.setOnClickListener(v -> {
            if (audioPlayer != null) {
                audioPlayer.start();
            }

            audioManager.reproducirClic();
        });

        setupOption(option1,
                "Hola",
                R.raw.voz_yaw);

        setupOption(option2,
                "Buenas noches",
                R.raw.voz_allin_chisiyay);

        setupOption(option3,
                "¿Cómo está?",
                R.raw.voz_imaynalla);

        setupOption(option4,
                "Muy bien",
                R.raw.voz_walliqlla);

        checkButton.setOnClickListener(v -> {

            if (selectedAnswer.isEmpty()) return;

            if (normalize(selectedAnswer)
                    .equals(normalize(correctAnswer))) {

                long duracion =
                        System.currentTimeMillis() - tiempoInicio;

                audioManager.reproducirExito();

                showDialog(
                        "¡Bien hecho!",
                        "¡Respuesta correcta! 🎉",
                        duracion
                );

            } else {

                audioManager.reproducirError();
                restarVida();

            }
        });
    }

    private void mostrarDialogoSalir() {

        if (audioManager != null)
            audioManager.reproducirClic();

        new AlertDialog.Builder(this)
                .setTitle("¿Quieres salir? 😟")
                .setMessage("¿Estás seguro de que no quieres seguir con la lección?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {

                    Intent intent =
                            new Intent(
                                    ejercicio2_1.this,
                                    homeActivity.class
                            );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

                    startActivity(intent);
                    finish();

                })
                .setNegativeButton("Cancelar",
                        (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    @Override
    public void onBackPressed() {
        mostrarDialogoSalir();
    }

    private void restarVida() {

        vidasActuales =
                Math.max(vidasActuales - 1, 0);

        guardarVidas();

        vidasCount.setText(
                String.valueOf(vidasActuales)
        );

        if (vidasActuales <= 0) {

            Toast.makeText(
                    this,
                    "💔 Sin vidas. Espera 1 hora.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent =
                    new Intent(
                            this,
                            homeActivity.class
                    );

            startActivity(intent);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "❌ Incorrecto. Te quedan "
                            + vidasActuales +
                            " vidas.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void guardarVidas() {

        SharedPreferences prefs =
                getSharedPreferences(
                        "game_data",
                        MODE_PRIVATE
                );

        prefs.edit()
                .putLong("vidas", vidasActuales)
                .apply();
    }

    private void setupOption(
            LinearLayout layout,
            String answer,
            int audioResId
    ) {

        layout.setOnClickListener(v -> {

            clearSelections();

            layout.setSelected(true);

            selectedAnswer = answer;

            checkButton.setEnabled(true);

            checkButton.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#1E1E24")
                    )
            );

            checkButton.setTextColor(
                    Color.parseColor("#FFFFFF")
            );

            if (optionPlayer != null) {
                optionPlayer.release();
            }

            optionPlayer =
                    MediaPlayer.create(
                            this,
                            audioResId
                    );

            if (optionPlayer != null) {
                optionPlayer.start();
            }

            audioManager.reproducirClic();
        });
    }

    private void clearSelections() {

        option1.setSelected(false);
        option2.setSelected(false);
        option3.setSelected(false);
        option4.setSelected(false);
    }

    private void showDialog(
            String title,
            String message,
            long duracion
    ) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "Continuar",
                        (dialog, which) -> {

                            guardarVidas();

                            Intent intent =
                                    new Intent(
                                            this,
                                            ejercicio2_2.class
                                    );

                            intent.putExtra(
                                    "exp",
                                    10
                            );

                            intent.putExtra(
                                    "tiempo",
                                    duracion
                            );

                            intent.putExtra(
                                    "nivel",
                                    2
                            );

                            intent.putExtra(
                                    "ejercicio",
                                    2
                            );

                            intent.putExtra(
                                    "vidas",
                                    vidasActuales
                            );

                            startActivity(intent);
                            finish();
                        }
                )
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
    protected void onPause() {
        super.onPause();

        if (audioPlayer != null &&
                audioPlayer.isPlaying()) {
            audioPlayer.pause();
        }

        if (optionPlayer != null &&
                optionPlayer.isPlaying()) {
            optionPlayer.pause();
        }

        guardarVidas();
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
}