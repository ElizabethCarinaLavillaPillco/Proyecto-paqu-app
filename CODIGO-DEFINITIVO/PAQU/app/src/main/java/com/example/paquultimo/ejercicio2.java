package com.example.paquultimo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ejercicio2 extends AppCompatActivity {

    FlexboxLayout oracionContainer;
    Button checkButton;

    LinearLayout opcion1, opcion2, opcion3, opcion4;
    List<String> oracionActual = new ArrayList<>();
    Map<String, LinearLayout> palabraLayouts = new HashMap<>();

    String oracionCorrecta = "Allin sukha";
    MediaPlayer audio;
    long tiempoInicio;
    int expAcumulada;
    long tiempoAcumulado;
    long vidasActuales = 5;
    TextView vidasCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio2);

        // Referencias UI
        oracionContainer = findViewById(R.id.oracionContainer);
        checkButton = findViewById(R.id.checkButton);
        vidasCount = findViewById(R.id.livesCount);
        checkButton.setEnabled(false);

        // 🟪 ProgressBar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(2 * 20);

        // Recibir datos del intent
        expAcumulada = getIntent().getIntExtra("exp", 0);
        tiempoAcumulado = getIntent().getLongExtra("tiempo", 0);
        vidasActuales = getIntent().getLongExtra("vidas", 5);
        vidasCount.setText(String.valueOf(vidasActuales));

        if (vidasActuales <= 0) {
            Toast.makeText(this, "💔 Sin vidas. Intenta más tarde.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Tiempo
        tiempoInicio = System.currentTimeMillis();

        // Audio
        LinearLayout audioButton = findViewById(R.id.audioButton);
        audio = MediaPlayer.create(this, R.raw.voz_buenas_tardes);
        audioButton.setOnClickListener(v -> audio.start());

        // Opciones
        opcion1 = findViewById(R.id.opcion1);
        opcion2 = findViewById(R.id.opcion2);
        opcion3 = findViewById(R.id.opcion3);
        opcion4 = findViewById(R.id.opcion4);

        setupOption(opcion1, "Allin", R.raw.voz_buenas);
        setupOption(opcion2, "p'unchay", R.raw.voz_punchay);
        setupOption(opcion3, "sukha", R.raw.voz_sukha);
        setupOption(opcion4, "tuta", R.raw.voz_tuta);

        checkButton.setOnClickListener(v -> verificarRespuesta());
    }

    private void setupOption(LinearLayout layout, String palabra, int audioResId) {
        palabraLayouts.put(palabra, layout);
        layout.setOnClickListener(v -> {
            if (!oracionActual.contains(palabra)) {
                oracionActual.add(palabra);
                layout.setAlpha(0.4f);
                añadirPalabraOracion(palabra);
                checkButton.setEnabled(true);
                if (audio != null) audio.release();
                audio = MediaPlayer.create(this, audioResId);
                audio.start();
            }
        });
    }

    private void añadirPalabraOracion(String palabra) {
        TextView palabraView = new TextView(this);
        palabraView.setText(palabra);
        palabraView.setTextSize(16f);
        palabraView.setPadding(16, 8, 16, 8);
        palabraView.setBackgroundResource(R.drawable.option_background);
        palabraView.setTextColor(Color.BLACK);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8);
        palabraView.setLayoutParams(params);

        palabraView.setOnClickListener(v -> {
            oracionActual.remove(palabra);
            oracionContainer.removeView(palabraView);
            palabraLayouts.get(palabra).setAlpha(1f);
            if (oracionActual.isEmpty()) checkButton.setEnabled(false);
        });

        oracionContainer.addView(palabraView);
    }

    private void verificarRespuesta() {
        long tiempoFin = System.currentTimeMillis();
        long duracion = tiempoFin - tiempoInicio;

        String fraseUsuario = TextUtils.join(" ", oracionActual).trim();
        if (normalize(fraseUsuario).equals(normalize(oracionCorrecta))) {
            int exp = 10;
            int expTotal = expAcumulada + exp;
            long tiempoTotal = tiempoAcumulado + duracion;

            showDialog("✅ ¡Bien hecho!", "¡Oración correcta!", expTotal, tiempoTotal);
        } else {
            restarVida();
        }
    }

    private void restarVida() {
        vidasActuales = Math.max(vidasActuales - 1, 0);
        vidasCount.setText(String.valueOf(vidasActuales));

        if (vidasActuales <= 0) {
            Toast.makeText(this, "💔 Sin vidas. Intenta más tarde.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "❌ Incorrecto. Te quedan " + vidasActuales + " vidas.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog(String title, String message, int expTotal, long tiempoTotal) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Finalizar", (dialog, which) -> {
                    // Solo cierra el diálogo
                    dialog.dismiss();
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
    protected void onDestroy() {
        if (audio != null) {
            audio.release();
            audio = null;
        }
        super.onDestroy();
    }
}
