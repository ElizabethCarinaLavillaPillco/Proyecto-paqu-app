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

public class ejercicio4_1 extends AppCompatActivity {

    long tiempoInicio;

    LinearLayout opcion1, opcion2, opcion3, opcion4, opcion5,
            opcion6, opcion7, opcion8, opcion9, opcion10;

    Button checkButton;
    ImageButton btnBackToMenu;

    String correctAnswer = "Pusaq";
    String selectedAnswer = "";

    MediaPlayer audioPlayer, optionPlayer;

    long vidasActuales = 5;
    TextView vidasCount;

    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio4_1);

        audioManager = AudioManager.getInstance(this);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(20);

        vidasCount = findViewById(R.id.livesCount);

        vidasActuales = getIntent().getLongExtra("vidas", 5);
        vidasCount.setText(String.valueOf(vidasActuales));

        guardarVidas();

        if (vidasActuales <= 0) {
            Toast.makeText(this,
                    "💔 Sin vidas. Intenta más tarde.",
                    Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        tiempoInicio = System.currentTimeMillis();

        opcion1 = findViewById(R.id.opcion1);
        opcion2 = findViewById(R.id.opcion2);
        opcion3 = findViewById(R.id.opcion3);
        opcion4 = findViewById(R.id.opcion4);
        opcion5 = findViewById(R.id.opcion5);
        opcion6 = findViewById(R.id.opcion6);
        opcion7 = findViewById(R.id.opcion7);
        opcion8 = findViewById(R.id.opcion8);
        opcion9 = findViewById(R.id.opcion9);
        opcion10 = findViewById(R.id.opcion10);

        checkButton = findViewById(R.id.checkButton);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        btnBackToMenu.setOnClickListener(v -> mostrarDialogoSalir());

        LinearLayout audioButton = findViewById(R.id.audioButton);

        audioPlayer = MediaPlayer.create(this, R.raw.voz_ocho);

        audioButton.setOnClickListener(v -> {
            if (audioPlayer != null) {
                audioPlayer.start();
            }
            audioManager.reproducirClic();
        });

        setupOption(opcion1, "Huk", R.raw.voz_uno);
        setupOption(opcion2, "Iskay", R.raw.voz_dos);
        setupOption(opcion3, "Kimsa", R.raw.voz_tres);
        setupOption(opcion4, "Tawa", R.raw.voz_cuatro);
        setupOption(opcion5, "Pisqa", R.raw.voz_cinco);
        setupOption(opcion6, "Soqta", R.raw.voz_seis);
        setupOption(opcion7, "Qanchis", R.raw.voz_siete);
        setupOption(opcion8, "Pusaq", R.raw.voz_ocho);
        setupOption(opcion9, "Isqun", R.raw.voz_nueve);
        setupOption(opcion10, "Chunka", R.raw.voz_diez);

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

        new AlertDialog.Builder(this)
                .setTitle("¿Quieres salir? 😟")
                .setMessage("¿Estás seguro de que no quieres seguir con la lección?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {

                    Intent intent =
                            new Intent(
                                    ejercicio4_1.this,
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
                .show();
    }

    @Override
    public void onBackPressed() {
        mostrarDialogoSalir();
    }

    private void restarVida() {

        vidasActuales = Math.max(
                vidasActuales - 1,
                0
        );

        guardarVidas();

        vidasCount.setText(
                String.valueOf(vidasActuales)
        );

        if (vidasActuales <= 0) {

            Toast.makeText(this,
                    "💔 Sin vidas. Intenta más tarde.",
                    Toast.LENGTH_LONG).show();

            startActivity(
                    new Intent(
                            this,
                            homeActivity.class
                    )
            );

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
                    Color.WHITE
            );

            if (optionPlayer != null) {
                optionPlayer.release();
            }

            optionPlayer =
                    MediaPlayer.create(
                            this,
                            audioResId
                    );

            optionPlayer.start();

            audioManager.reproducirClic();
        });
    }

    private void clearSelections() {

        opcion1.setSelected(false);
        opcion2.setSelected(false);
        opcion3.setSelected(false);
        opcion4.setSelected(false);
        opcion5.setSelected(false);
        opcion6.setSelected(false);
        opcion7.setSelected(false);
        opcion8.setSelected(false);
        opcion9.setSelected(false);
        opcion10.setSelected(false);
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
                                            ejercicio4_2.class
                                    );

                            intent.putExtra("exp", 10);
                            intent.putExtra("tiempo", duracion);
                            intent.putExtra("nivel", 4);
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
                .replace("’", "'")
                .replace("‘", "'")
                .replace("`", "'")
                .replace("´", "'")
                .replace("ʻ", "'");
    }

    @Override
    protected void onPause() {
        super.onPause();
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