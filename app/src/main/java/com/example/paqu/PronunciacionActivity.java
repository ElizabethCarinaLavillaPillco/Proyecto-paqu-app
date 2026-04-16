package com.example.paqu;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
//import java.util.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PronunciacionActivity extends AppCompatActivity {

    // UI Components
    ImageView btnBack;
    TextView tvPalabraActual, tvTraduccion, tvInstrucciones, tvResultado, tvPuntaje;
    MaterialButton btnGrabar, btnSiguiente, btnRepetir;
    ProgressBar progressBar;
    MaterialCardView cardPalabra, cardResultado;
    LottieAnimationView lottieAnimacion;
    View indicadorNivel;

    // Audio
    private static final int REQUEST_RECORD_AUDIO = 1;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File audioFile;

    // Palabras para practicar (Quechua Cusqueño)
    private List<PalabraPronunciacion> palabras;
    private int indiceActual = 0;
    private int puntajeTotal = 0;

    // Threading
    ExecutorService executor;
    Handler mainHandler;

    // API Configuration
    private static final String API_URL = "http://127.0.0.1:5000/pronunciacion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciacion);

        initViews();
        initPalabras();
        setupListeners();
        checkPermissions();
        animacionEntrada();

        //executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(getMainLooper());

        mostrarPalabraActual();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPalabraActual = findViewById(R.id.tvPalabraActual);
        tvTraduccion = findViewById(R.id.tvTraduccion);
        tvInstrucciones = findViewById(R.id.tvInstrucciones);
        tvResultado = findViewById(R.id.tvResultado);
        tvPuntaje = findViewById(R.id.tvPuntaje);

        btnGrabar = findViewById(R.id.btnGrabar);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnRepetir = findViewById(R.id.btnRepetir);

        progressBar = findViewById(R.id.progressBar);
        cardPalabra = findViewById(R.id.cardPalabra);
        cardResultado = findViewById(R.id.cardResultado);
        lottieAnimacion = findViewById(R.id.lottieAnimacion);
        indicadorNivel = findViewById(R.id.indicadorNivel);

        cardResultado.setVisibility(View.GONE);
    }

    private void initPalabras() {
        palabras = new ArrayList<>();
        palabras.add(new PalabraPronunciacion("Allinllachu", "¿Cómo estás?", 1));
        palabras.add(new PalabraPronunciacion("Añay", "¡Qué lindo!", 1));
        palabras.add(new PalabraPronunciacion("Inti", "Sol", 1));
        palabras.add(new PalabraPronunciacion("Mama Quilla", "Madre Luna", 2));
        palabras.add(new PalabraPronunciacion("Sumaq kawsay", "Buen vivir", 2));
        palabras.add(new PalabraPronunciacion("Ñuqanchik", "Nosotros/as", 2));
        palabras.add(new PalabraPronunciacion("Qhapaq", "Rico/Poderoso", 3));
        palabras.add(new PalabraPronunciacion("Tukuy Sunqu", "Con todo el corazón", 3));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            animarClick(v);
            finish();
        });

        btnGrabar.setOnClickListener(v -> {
            if (!isRecording) {
                iniciarGrabacion();
            } else {
                detenerGrabacion();
            }
        });

        btnSiguiente.setOnClickListener(v -> {
            animarClick(v);
            siguientePalabra();
        });

        btnRepetir.setOnClickListener(v -> {
            animarClick(v);
            repetirPalabra();
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    private void mostrarPalabraActual() {
        if (indiceActual < palabras.size()) {
            PalabraPronunciacion palabra = palabras.get(indiceActual);
            tvPalabraActual.setText(palabra.palabra);
            tvTraduccion.setText(palabra.traduccion);
            tvInstrucciones.setText("🎤 Pronuncia: " + palabra.palabra);
            tvPuntaje.setText("Puntaje: " + puntajeTotal);

            actualizarIndicadorNivel(palabra.nivel);
            cardResultado.setVisibility(View.GONE);
            btnSiguiente.setEnabled(false);

            animarPalabra();
        } else {
            mostrarResultadoFinal();
        }
    }

    private void iniciarGrabacion() {
        try {
            audioFile = new File(getCacheDir(), "pronunciacion_temp.pcm");

            int bufferSize = AudioRecord.getMinBufferSize(
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "⚠️ Permiso de audio requerido", Toast.LENGTH_SHORT).show();
                return;
            }

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            audioRecord.startRecording();
            isRecording = true;

            btnGrabar.setText("⏹ DETENER");
            btnGrabar.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            lottieAnimacion.playAnimation();

            Toast.makeText(this, "🎤 Grabando...", Toast.LENGTH_SHORT).show();

            // Grabar audio en background
            executor.execute(() -> {
                try {
                    FileOutputStream fos = new FileOutputStream(audioFile);
                    byte[] buffer = new byte[bufferSize];

                    while (isRecording) {
                        int read = audioRecord.read(buffer, 0, buffer.length);
                        if (read > 0) {
                            fos.write(buffer, 0, read);
                        }
                    }

                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Error al grabar", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerGrabacion() {
        if (isRecording && audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;

            btnGrabar.setText("🎤 GRABAR");
            btnGrabar.setBackgroundColor(getResources().getColor(R.color.morado));
            lottieAnimacion.pauseAnimation();

            // Enviar audio a la API para evaluación
            evaluarPronunciacion();
        }
    }

    private void evaluarPronunciacion() {
        progressBar.setVisibility(View.VISIBLE);
        tvInstrucciones.setText("🔄 Evaluando pronunciación...");

        executor.execute(() -> {
            try {
                // Simular evaluación (reemplazar con API real)
                Thread.sleep(2000);

                // Resultado simulado
                int puntaje = (int) (Math.random() * 40) + 60; // 60-100
                String feedback = generarFeedback(puntaje);

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    mostrarResultado(puntaje, feedback);
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "❌ Error al evaluar", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String generarFeedback(int puntaje) {
        if (puntaje >= 90) return "¡Excelente! 🌟";
        else if (puntaje >= 75) return "¡Muy bien! 👍";
        else if (puntaje >= 60) return "Bien, sigue practicando 💪";
        else return "Intenta nuevamente 🔄";
    }

    private void mostrarResultado(int puntaje, String feedback) {
        cardResultado.setVisibility(View.VISIBLE);
        tvResultado.setText(feedback + "\nPuntaje: " + puntaje + "/100");

        puntajeTotal += puntaje;
        tvPuntaje.setText("Puntaje Total: " + puntajeTotal);

        btnSiguiente.setEnabled(true);
        animarCardResultado();
    }

    private void siguientePalabra() {
        indiceActual++;
        mostrarPalabraActual();
    }

    private void repetirPalabra() {
        cardResultado.setVisibility(View.GONE);
        btnSiguiente.setEnabled(false);
    }

    private void mostrarResultadoFinal() {
        tvPalabraActual.setText("¡Completado!");
        tvTraduccion.setText("Puntaje Final: " + puntajeTotal + "/" + (palabras.size() * 100));
        tvInstrucciones.setText("🎉 ¡Excelente trabajo!");
        btnGrabar.setEnabled(false);
        cardResultado.setVisibility(View.GONE);
    }

    private void actualizarIndicadorNivel(int nivel) {
        int color = nivel == 1 ? android.R.color.holo_green_light :
                nivel == 2 ? android.R.color.holo_orange_light :
                        android.R.color.holo_red_light;
        indicadorNivel.setBackgroundColor(getResources().getColor(color));
    }

    // ============= ANIMACIONES =============

    private void animacionEntrada() {
        View[] vistas = {cardPalabra, btnGrabar};
        for (int i = 0; i < vistas.length; i++) {
            View vista = vistas[i];
            vista.setAlpha(0f);
            vista.setTranslationY(100f);

            vista.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(i * 100)
                    .setInterpolator(new BounceInterpolator())
                    .start();
        }
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
    }

    private void animarPalabra() {
        tvPalabraActual.setAlpha(0f);
        tvPalabraActual.setScaleX(0.5f);
        tvPalabraActual.setScaleY(0.5f);

        tvPalabraActual.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new BounceInterpolator())
                .start();
    }

    private void animarCardResultado() {
        cardResultado.setAlpha(0f);
        cardResultado.animate()
                .alpha(1f)
                .setDuration(400)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.release();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    // Clase auxiliar
    static class PalabraPronunciacion {
        String palabra;
        String traduccion;
        int nivel; // 1=Fácil, 2=Medio, 3=Difícil

        PalabraPronunciacion(String palabra, String traduccion, int nivel) {
            this.palabra = palabra;
            this.traduccion = traduccion;
            this.nivel = nivel;
        }
    }
}