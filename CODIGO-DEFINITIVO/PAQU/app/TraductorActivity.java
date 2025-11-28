package com.example.paqu;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TraductorActivity extends AppCompatActivity {

    // UI Components
    EditText etTextoOrigen;
    TextView tvTextoTraducido, tvContadorCaracteres, tvIdiomaSalida;
    MaterialButton btnTraducir, btnIntercambiar, btnLimpiar, btnCopiar;
    ImageView btnAudioOrigen, btnAudioTraducido, iconAtras, iconMicrofono;
    ProgressBar progressBar;
    MaterialCardView cardOrigen, cardTraducido;
    View indicadorIdioma;

    // Estado
    private boolean esQuechuaAEspanol = true; // true = Quechua→Español, false = Español→Quechua
    private String ultimaTraduccion = "";

    // Audio
    AudioManager audioManager;
    MediaPlayer audioTraduccion;

    // Threading
    Handler mainHandler;

    // API Configuration (cambiar por tu servidor)
    private static final String API_URL = "http://10.0.2.2:5000/traducir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traductor);

        // Inicializar
        initViews();
        setupListeners();
        animacionEntrada();

        // Threading
        mainHandler = new Handler(Looper.getMainLooper());

        // Audio
        audioManager = AudioManager.getInstance(this);
    }

    private void initViews() {
        // EditText y TextViews
        etTextoOrigen = findViewById(R.id.etTextoOrigen);
        tvTextoTraducido = findViewById(R.id.tvTextoTraducido);
        tvContadorCaracteres = findViewById(R.id.tvContadorCaracteres);
        tvIdiomaSalida = findViewById(R.id.tvIdiomaSalida);

        // Botones
        btnTraducir = findViewById(R.id.btnTraducir);
        btnIntercambiar = findViewById(R.id.btnIntercambiar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnCopiar = findViewById(R.id.btnCopiar);

        // ImageViews
        btnAudioOrigen = findViewById(R.id.btnAudioOrigen);
        btnAudioTraducido = findViewById(R.id.btnAudioTraducido);
        iconAtras = findViewById(R.id.iconAtras);
        iconMicrofono = findViewById(R.id.iconMicrofono);

        // Otros
        progressBar = findViewById(R.id.progressBar);
        cardOrigen = findViewById(R.id.cardOrigen);
        cardTraducido = findViewById(R.id.cardTraducido);
        indicadorIdioma = findViewById(R.id.indicadorIdioma);

        // Estado inicial
        actualizarUIIdioma();
    }

    private void setupListeners() {
        // Botón Atrás
        iconAtras.setOnClickListener(v -> {
            animarClick(v);
            finish();
        });

        // Contador de caracteres
        etTextoOrigen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int longitud = s.length();
                tvContadorCaracteres.setText(longitud + "/500");

                // Habilitar/deshabilitar botones
                boolean hayTexto = longitud > 0;
                btnTraducir.setEnabled(hayTexto);
                btnLimpiar.setEnabled(hayTexto);
                btnAudioOrigen.setEnabled(hayTexto);

                // Cambiar color según límite
                if (longitud > 450) {
                    tvContadorCaracteres.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (longitud > 350) {
                    tvContadorCaracteres.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    tvContadorCaracteres.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón Traducir
        btnTraducir.setOnClickListener(v -> {
            animarClick(v);
            audioManager.reproducirClic();
            traducirTexto();
        });

        // Botón Intercambiar Idiomas
        btnIntercambiar.setOnClickListener(v -> {
            animarRotacion(v);
            audioManager.reproducirClic();
            intercambiarIdiomas();
        });

        // Botón Limpiar
        btnLimpiar.setOnClickListener(v -> {
            animarClick(v);
            audioManager.reproducirClic();
            limpiarCampos();
        });

        // Botón Copiar
        btnCopiar.setOnClickListener(v -> {
            animarClick(v);
            audioManager.reproducirClic();
            copiarTraduccion();
        });

        // Audio Origen
        btnAudioOrigen.setOnClickListener(v -> {
            animarClick(v);
            reproducirAudioOrigen();
        });

        // Audio Traducido
        btnAudioTraducido.setOnClickListener(v -> {
            animarClick(v);
            reproducirAudioTraducido();
        });

        // Micrófono (Voice Input) - Próxima implementación
        iconMicrofono.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "🎤 Función de voz próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    // ============= FUNCIONALIDAD PRINCIPAL =============

    private void traducirTexto() {
        String textoOrigen = etTextoOrigen.getText().toString().trim();

        if (textoOrigen.isEmpty()) {
            Toast.makeText(this, "⚠️ Ingresa un texto para traducir", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar loading
        progressBar.setVisibility(View.VISIBLE);
        btnTraducir.setEnabled(false);
        tvTextoTraducido.setText("Traduciendo...");
        animarCard(cardTraducido);

        // Llamada a la API en background
        new Thread(() -> {
            try {
                String traduccion = llamarAPITraduccion(textoOrigen, esQuechuaAEspanol);

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTraducir.setEnabled(true);

                    if (traduccion != null && !traduccion.isEmpty()) {
                        ultimaTraduccion = traduccion;
                        tvTextoTraducido.setText(traduccion);
                        btnCopiar.setEnabled(true);
                        btnAudioTraducido.setEnabled(true);
                        animarTextoTraducido();
                        audioManager.reproducirExito();
                    } else {
                        tvTextoTraducido.setText("❌ Error al traducir");
                        audioManager.reproducirError();
                        Toast.makeText(this, "Error en la traducción", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTraducir.setEnabled(true);
                    tvTextoTraducido.setText("❌ Error de conexión");
                    audioManager.reproducirError();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String llamarAPITraduccion(String texto, boolean quechuaAEspanol) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000); // 10 segundos
        conn.setReadTimeout(10000);

        // Crear JSON request
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("texto", texto);
        jsonRequest.put("de", quechuaAEspanol ? "qu" : "es");
        jsonRequest.put("a", quechuaAEspanol ? "es" : "qu");

        // Enviar request
        OutputStream os = conn.getOutputStream();
        os.write(jsonRequest.toString().getBytes("UTF-8"));
        os.close();

        // Leer respuesta
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("traduccion");
        } else {
            throw new Exception("Error HTTP: " + responseCode);
        }
    }

    private void intercambiarIdiomas() {
        esQuechuaAEspanol = !esQuechuaAEspanol;
        actualizarUIIdioma();

        // Intercambiar textos
        String temp = etTextoOrigen.getText().toString();
        etTextoOrigen.setText(ultimaTraduccion);
        ultimaTraduccion = temp;
        tvTextoTraducido.setText(temp);
    }

    @SuppressLint("SetTextI18n")
    private void actualizarUIIdioma() {
        if (esQuechuaAEspanol) {
            etTextoOrigen.setHint("Escribe en Quechua...");
            tvIdiomaSalida.setText("🇵🇪 Español");
            animarIndicador(0); // Izquierda
        } else {
            etTextoOrigen.setHint("Escribe en Español...");
            tvIdiomaSalida.setText("🏔️ Quechua");
            animarIndicador(1); // Derecha
        }
    }

    private void limpiarCampos() {
        etTextoOrigen.setText("");
        tvTextoTraducido.setText("La traducción aparecerá aquí");
        ultimaTraduccion = "";
        btnCopiar.setEnabled(false);
        btnAudioTraducido.setEnabled(false);
    }

    private void copiarTraduccion() {
        if (!ultimaTraduccion.isEmpty()) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Traducción", ultimaTraduccion);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "✅ Texto copiado", Toast.LENGTH_SHORT).show();
            animarBoton(btnCopiar);
        }
    }

    // ============= AUDIO =============

    private void reproducirAudioOrigen() {
        String texto = etTextoOrigen.getText().toString();
        if (!texto.isEmpty()) {
            // TODO: Implementar TTS para Quechua/Español
            Toast.makeText(this, "🔊 Reproduciendo: " + texto, Toast.LENGTH_SHORT).show();
            audioManager.reproducirClic();
        }
    }

    private void reproducirAudioTraducido() {
        if (!ultimaTraduccion.isEmpty()) {
            // TODO: Implementar TTS
            Toast.makeText(this, "🔊 Reproduciendo: " + ultimaTraduccion, Toast.LENGTH_SHORT).show();
            audioManager.reproducirClic();
        }
    }

    // ============= ANIMACIONES =============

    private void animacionEntrada() {
        View[] vistas = {cardOrigen, cardTraducido, btnTraducir, btnIntercambiar};

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

    private void animarRotacion(View view) {
        view.animate()
                .rotationBy(180f)
                .setDuration(400)
                .start();
    }

    private void animarCard(View card) {
        ObjectAnimator.ofFloat(card, "elevation", 4f, 12f, 4f)
                .setDuration(500)
                .start();
    }

    private void animarTextoTraducido() {
        tvTextoTraducido.setAlpha(0f);
        tvTextoTraducido.animate()
                .alpha(1f)
                .setDuration(400)
                .start();
    }

    private void animarBoton(Button btn) {
        btn.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(150)
                .withEndAction(() ->
                        btn.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                ).start();
    }

    private void animarIndicador(int posicion) {
        float translationX = posicion == 0 ? 0f : indicadorIdioma.getWidth();
        indicadorIdioma.animate()
                .translationX(translationX)
                .setDuration(300)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioTraduccion != null) {
            audioTraduccion.release();
        }
    }
}