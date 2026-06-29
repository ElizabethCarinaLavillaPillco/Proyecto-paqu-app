package com.example.paqu;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.paqu.utils.DiccionarioQuechuaLocal;
import com.example.paqu.utils.QuechuaTTSManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TraductorActivity extends AppCompatActivity {

    private static final String TAG = "TraductorActivity";

    // UI Components
    EditText etTextoOrigen;
    TextView tvTextoTraducido, tvContadorCaracteres, tvIdiomaSalida, tvEstadoConexion;
    MaterialButton btnTraducir, btnIntercambiar, btnLimpiar, btnCopiar;
    ImageView btnAudioOrigen, btnAudioTraducido, iconAtras, iconMicrofono;
    ProgressBar progressBar;
    MaterialCardView cardOrigen, cardTraducido;
    View indicadorIdioma;

    // Estado
    private boolean esQuechuaAEspanol = true;
    private String ultimaTraduccion = "";

    // 🔊 TTS Manager
    private QuechuaTTSManager ttsManager;

    // Threading
    Handler mainHandler;

    // Diccionario local
    DiccionarioQuechuaLocal diccionarioLocal;

    // 🔑 API Configuration - HUGGING FACE
    // ⚠️ IMPORTANTE: Verifica esta URL en tu navegador primero
    private static final String HF_SPACE_URL = "https://kawazzzaki-traductor-qu-es.hf.space";

    // 🔍 Endpoint correcto para Gradio 4.x/5.x
    private static final String API_ENDPOINT = HF_SPACE_URL + "/api/predict";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traductor);

        initViews();
        setupListeners();
        animacionEntrada();

        mainHandler = new Handler(Looper.getMainLooper());

        // 🔊 Inicializar TTS Manager
        ttsManager = new QuechuaTTSManager(this);

        diccionarioLocal = DiccionarioQuechuaLocal.getInstance();

        Toast.makeText(this,
                "📚 Diccionario: " + diccionarioLocal.getTamanoDiccionario() + " palabras\n🔊 TTS listo",
                Toast.LENGTH_SHORT).show();

        // 🔍 Verificar conexión
        verificarConexionServidor();
    }

    private void initViews() {
        etTextoOrigen = findViewById(R.id.etTextoOrigen);
        tvTextoTraducido = findViewById(R.id.tvTextoTraducido);
        tvContadorCaracteres = findViewById(R.id.tvContadorCaracteres);
        tvIdiomaSalida = findViewById(R.id.tvIdiomaSalida);
        tvEstadoConexion = findViewById(R.id.tvEstadoLeccion);

        btnTraducir = findViewById(R.id.btnTraducir);
        btnIntercambiar = findViewById(R.id.btnIntercambiar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnCopiar = findViewById(R.id.btnCopiar);

        btnAudioOrigen = findViewById(R.id.btnAudioOrigen);
        btnAudioTraducido = findViewById(R.id.btnAudioTraducido);
        iconAtras = findViewById(R.id.iconAtras);
        iconMicrofono = findViewById(R.id.iconMicrofono);

        progressBar = findViewById(R.id.progressBar);
        cardOrigen = findViewById(R.id.cardOrigen);
        cardTraducido = findViewById(R.id.cardTraducido);
        indicadorIdioma = findViewById(R.id.indicadorIdioma);

        actualizarUIIdioma();
    }

    private void setupListeners() {
        iconAtras.setOnClickListener(v -> {
            animarClick(v);
            finish();
        });

        etTextoOrigen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int longitud = s.length();
                tvContadorCaracteres.setText(longitud + "/500");

                boolean hayTexto = longitud > 0;
                btnTraducir.setEnabled(hayTexto);
                btnLimpiar.setEnabled(hayTexto);
                btnAudioOrigen.setEnabled(hayTexto);

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

        btnTraducir.setOnClickListener(v -> {
            animarClick(v);
            traducirTexto();
        });

        btnIntercambiar.setOnClickListener(v -> {
            animarRotacion(v);
            intercambiarIdiomas();
        });

        btnLimpiar.setOnClickListener(v -> {
            animarClick(v);
            limpiarCampos();
        });

        btnCopiar.setOnClickListener(v -> {
            animarClick(v);
            copiarTraduccion();
        });

        // 🔊 Botón Audio Origen
        btnAudioOrigen.setOnClickListener(v -> {
            animarClick(v);
            String texto = etTextoOrigen.getText().toString().trim();
            if (!texto.isEmpty()) {
                reproducirConTTS(texto, esQuechuaAEspanol ? "qu" : "es");
            }
        });

        // 🔊 Botón Audio Traducido
        btnAudioTraducido.setOnClickListener(v -> {
            animarClick(v);
            if (!ultimaTraduccion.isEmpty()) {
                reproducirConTTS(ultimaTraduccion, esQuechuaAEspanol ? "es" : "qu");
            }
        });

        iconMicrofono.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "🎤 Función de voz próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    // ============= FUNCIONALIDAD PRINCIPAL =============

    private void traducirTexto() {
        String textoOrigen = etTextoOrigen.getText().toString().trim();

        if (textoOrigen.isEmpty()) {
            Toast.makeText(this, "⚠️ Ingresa un texto", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnTraducir.setEnabled(false);
        tvTextoTraducido.setText("Traduciendo...");
        animarCard(cardTraducido);

        // ⭐ INTENTAR PRIMERO CON DICCIONARIO LOCAL ⭐
        String traduccionLocal = diccionarioLocal.traducirFrase(textoOrigen, esQuechuaAEspanol);

        if (traduccionLocal != null && !traduccionLocal.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            btnTraducir.setEnabled(true);
            ultimaTraduccion = traduccionLocal;
            tvTextoTraducido.setText(traduccionLocal);
            btnCopiar.setEnabled(true);
            btnAudioTraducido.setEnabled(true);
            animarTextoTraducido();

            if (tvEstadoConexion != null) {
                tvEstadoConexion.setText("📚 Diccionario local");
                tvEstadoConexion.setVisibility(View.VISIBLE);
            }

            Toast.makeText(this, "✅ Traducción offline", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⭐ LLAMAR A LA API DE HUGGING FACE ⭐
        new Thread(() -> {
            try {
                Log.d(TAG, "🌐 Llamando API: " + API_ENDPOINT);
                String traduccion = llamarAPIHuggingFace(textoOrigen, esQuechuaAEspanol);

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTraducir.setEnabled(true);

                    if (traduccion != null && !traduccion.isEmpty() && !traduccion.startsWith("Error")) {
                        ultimaTraduccion = traduccion;
                        tvTextoTraducido.setText(traduccion);
                        btnCopiar.setEnabled(true);
                        btnAudioTraducido.setEnabled(true);
                        animarTextoTraducido();

                        if (tvEstadoConexion != null) {
                            tvEstadoConexion.setText("🌐 NLLB (Hugging Face)");
                            tvEstadoConexion.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(this, "✅ Traducción online", Toast.LENGTH_SHORT).show();
                    } else {
                        mostrarErrorSinTraduccion("Error: " + traduccion);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Error API: " + e.getMessage(), e);
                e.printStackTrace();
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTraducir.setEnabled(true);
                    mostrarErrorConexion(e.getMessage());
                });
            }
        }).start();
    }

    // 🔑 LLAMAR API HUGGING FACE
    private String llamarAPIHuggingFace(String texto, boolean quechuaAEspanol) throws Exception {
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);

        // Formato Gradio 4.x/5.x
        JSONObject jsonRequest = new JSONObject();
        JSONArray dataArray = new JSONArray();
        dataArray.put(texto);
        dataArray.put(quechuaAEspanol ? "qu" : "es");
        dataArray.put(quechuaAEspanol ? "es" : "qu");
        jsonRequest.put("data", dataArray);
        jsonRequest.put("fn_index", 0);

        Log.d(TAG, "📤 Request: " + jsonRequest.toString());

        // Enviar
        OutputStream os = conn.getOutputStream();
        os.write(jsonRequest.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        // Leer respuesta
        int responseCode = conn.getResponseCode();
        Log.d(TAG, "📥 Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            conn.disconnect();

            Log.d(TAG, "📥 Response: " + response.toString());

            JSONObject jsonResponse = new JSONObject(response.toString());

            if (jsonResponse.has("error") && !jsonResponse.isNull("error")) {
                return "Error: " + jsonResponse.getString("error");
            }

            if (jsonResponse.has("data")) {
                JSONArray data = jsonResponse.getJSONArray("data");
                if (data.length() > 0) {
                    return data.getString(0);
                }
            }

            return "Error: Respuesta vacía";
        } else {
            // Leer error
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line);
            }
            br.close();
            conn.disconnect();

            throw new Exception("HTTP " + responseCode + ": " + errorResponse.toString());
        }
    }

    // 🔊 REPRODUCIR CON TTS
    private void reproducirConTTS(String texto, String idioma) {
        // Determinar si es Quechua o Español
        boolean esQuechua = idioma.equals("qu");

        // Cambiar icono a "reproduciendo"
        ImageView btnAudio = esQuechua == esQuechuaAEspanol ? btnAudioOrigen : btnAudioTraducido;
        btnAudio.setImageResource(android.R.drawable.ic_media_pause);

        ttsManager.reproducir(texto, new QuechuaTTSManager.TTSCallback() {
            @Override
            public void onInicio() {
                runOnUiThread(() -> {
                    btnAudio.setEnabled(false);
                    btnAudio.setAlpha(0.5f);
                });
            }

            @Override
            public void onFin() {
                runOnUiThread(() -> {
                    btnAudio.setImageResource(esQuechua ? R.drawable.ic_audio : R.drawable.ic_audio);
                    btnAudio.setEnabled(true);
                    btnAudio.setAlpha(1.0f);
                });
            }

            @Override
            public void onError(String mensaje) {
                runOnUiThread(() -> {
                    btnAudio.setImageResource(esQuechua ? R.drawable.ic_audio : R.drawable.ic_audio);
                    btnAudio.setEnabled(true);
                    btnAudio.setAlpha(1.0f);
                    Toast.makeText(TraductorActivity.this, "❌ " + mensaje, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 🔍 VERIFICAR CONEXIÓN
    private void verificarConexionServidor() {
        new Thread(() -> {
            try {
                Log.d(TAG, "🔍 Verificando: " + HF_SPACE_URL);
                URL url = new URL(HF_SPACE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                conn.disconnect();

                Log.d(TAG, "✅ Servidor respondió: " + code);

                mainHandler.post(() -> {
                    if (code == 200) {
                        tvEstadoConexion.setText("🌐 Servidor online");
                        tvEstadoConexion.setVisibility(View.VISIBLE);
                    } else {
                        tvEstadoConexion.setText("⚠️ Código: " + code);
                        tvEstadoConexion.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "❌ Servidor offline: " + e.getMessage());
                mainHandler.post(() -> {
                    tvEstadoConexion.setText("❌ Sin conexión al servidor");
                    tvEstadoConexion.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void mostrarErrorSinTraduccion(String detalle) {
        tvTextoTraducido.setText("❌ No se pudo traducir\n\n" + detalle);

        if (tvEstadoConexion != null) {
            tvEstadoConexion.setText("📚 " + diccionarioLocal.getTamanoDiccionario() + " palabras offline");
            tvEstadoConexion.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarErrorConexion(String mensaje) {
        tvTextoTraducido.setText("❌ Error de conexión\n\n💡 Usa el diccionario local");

        if (tvEstadoConexion != null) {
            tvEstadoConexion.setText("❌ Offline");
            tvEstadoConexion.setVisibility(View.VISIBLE);
        }

        Toast.makeText(this, "💡 Error: " + mensaje, Toast.LENGTH_LONG).show();
    }

    private void intercambiarIdiomas() {
        esQuechuaAEspanol = !esQuechuaAEspanol;
        actualizarUIIdioma();

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
            animarIndicador(0);
        } else {
            etTextoOrigen.setHint("Escribe en Español...");
            tvIdiomaSalida.setText("🏔️ Quechua");
            animarIndicador(1);
        }
    }

    private void limpiarCampos() {
        etTextoOrigen.setText("");
        tvTextoTraducido.setText("La traducción aparecerá aquí");
        ultimaTraduccion = "";
        btnCopiar.setEnabled(false);
        btnAudioTraducido.setEnabled(false);

        if (tvEstadoConexion != null) {
            tvEstadoConexion.setVisibility(View.GONE);
        }
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
        view.animate().rotationBy(180f).setDuration(400).start();
    }

    private void animarCard(View card) {
        ObjectAnimator.ofFloat(card, "elevation", 4f, 12f, 4f).setDuration(500).start();
    }

    private void animarTextoTraducido() {
        tvTextoTraducido.setAlpha(0f);
        tvTextoTraducido.animate().alpha(1f).setDuration(400).start();
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
        indicadorIdioma.animate().translationX(translationX).setDuration(300).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) {
            ttsManager.detener();
        }
    }
}