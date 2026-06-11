package com.example.paqu;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class ejercicio4 extends AppCompatActivity {

    TextView tvPalabra, vidasCount;
    EditText oracionConstruida;
    Button checkButton;
    ImageButton btnBackToMenu;
    AudioManager audioManager;
    long tiempoInicio;
    int expAcumulada;
    long tiempoAcumulado;
    long vidasActuales = 5;

    String oracionCorrecta = "allin p'unchay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio4);

        // Referencias UI
        tvPalabra = findViewById(R.id.tvPalabra);
        oracionConstruida = findViewById(R.id.oracionConstruida);
        checkButton = findViewById(R.id.checkButton);
        vidasCount = findViewById(R.id.livesCount);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        audioManager = AudioManager.getInstance(this);

        // Control de salida estructurado
        btnBackToMenu.setOnClickListener(v -> mostrarDialogoSalir());

        // Configuración de datos iniciales
        tvPalabra.setText("Buenos días");

        // Recuperar información de la sesión anterior
        vidasActuales = getIntent().getLongExtra("vidas", 5);
        vidasCount.setText(String.valueOf(vidasActuales));

        guardarVidas();
        expAcumulada = getIntent().getIntExtra("exp", 0);
        tiempoAcumulado = getIntent().getLongExtra("tiempo", 0);

        if (vidasActuales <= 0) {
            Toast.makeText(this, "💔 Sin vidas. Intenta más tarde.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, homeActivity.class));
            finish();
            return;
        }

        // Progreso (Paso 4 de la lección)
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(4 * 20);

        // Tiempo de inicio del ejercicio
        tiempoInicio = System.currentTimeMillis();

        // MEJORA: Escucha interactiva del input de texto para reaccionar con el botón comprobar
        oracionConstruida.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    checkButton.setEnabled(false);
                    checkButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D6C7EB")));
                    checkButton.setTextColor(Color.parseColor("#A594BF"));
                } else {
                    checkButton.setEnabled(true);
                    checkButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E1E24")));
                    checkButton.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón comprobar
        checkButton.setOnClickListener(v -> {
            long tiempoFin = System.currentTimeMillis();
            long duracion = tiempoFin - tiempoInicio;

            String respuestaUsuario = oracionConstruida.getText().toString();

            if (normalize(respuestaUsuario).equals(normalize(oracionCorrecta))) {
                audioManager.reproducirExito();
                int exp = 10;
                int expTotal = expAcumulada + exp;
                long tiempoTotal = tiempoAcumulado + duracion;
                showDialog("✅ ¡Bien hecho!", "¡Buena respuesta!", expTotal, tiempoTotal);
            } else {
                restarVida();
            }
        });
    }

    private void mostrarDialogoSalir() {
        new AlertDialog.Builder(this)
                .setTitle("¿Quieres salir? 😟")
                .setMessage("¿Estás seguro de que no quieres seguir con la lección?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {

                    guardarVidas();

                    Intent intent = new Intent(ejercicio4.this, homeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    @Override
    public void onBackPressed() {
        mostrarDialogoSalir();
    }

    private void restarVida() {

        vidasActuales = Math.max(vidasActuales - 1, 0);

        guardarVidas();

        vidasCount.setText(String.valueOf(vidasActuales));

        if (vidasActuales <= 0) {

            Toast.makeText(this,
                    "💔 Te quedaste sin vidas. Regresando al inicio...",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, homeActivity.class);
            startActivity(intent);
            finish();

        } else {

            Toast.makeText(this,
                    "❌ Incorrecto. Te quedan " + vidasActuales + " vidas.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void guardarVidas() {
        SharedPreferences prefs = getSharedPreferences("game_data", MODE_PRIVATE);
        prefs.edit()
                .putLong("vidas", vidasActuales)
                .apply();
    }
    private void showDialog(String title, String message, int expTotal, long tiempoTotal) {

        guardarVidas();

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Finalizar", (dialog, which) -> {

                    Intent intent = new Intent(this, LeccionAcabadaActivity.class);

                    intent.putExtra("exp", expTotal);
                    intent.putExtra("tiempo", tiempoTotal);
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
        guardarVidas();
        super.onDestroy();
    }
}