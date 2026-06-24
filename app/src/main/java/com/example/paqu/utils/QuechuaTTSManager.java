package com.example.paqu.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager para generar y reproducir audio TTS Quechua desde el servidor Flask.
 *
 * USO BÁSICO:
 *   QuechuaTTSManager tts = new QuechuaTTSManager(context);
 *   tts.reproducir("Allin p'unchay", null);
 *
 * USO CON CALLBACK:
 *   tts.reproducir("Allin p'unchay", new QuechuaTTSManager.TTSCallback() {
 *       public void onInicio()   { btnAudio.setEnabled(false); }
 *       public void onFin()      { btnAudio.setEnabled(true);  }
 *       public void onError(String msg) { Toast.makeText(...).show(); }
 *   });
 *
 * CACHÉ:
 *   Los audios generados se guardan en caché local (carpeta "tts_cache").
 *   La segunda vez que se pide la misma palabra se reproduce instantáneamente.
 */
public class QuechuaTTSManager {

    private static final String TAG = "QuechuaTTS";

    // ⚠️ CAMBIA ESTA URL por la de tu servidor Flask
    // Emulador Android → host es 10.0.2.2
    // Dispositivo físico en la misma red → IP local de tu PC, ej: 192.168.1.X
    // Producción → tu dominio o ngrok URL
    // ✅ AHORA (Hugging Face Cloud)
    private static final String SERVER_URL = "https://kawazzzaki-tts-quechua-cusquito.hf.space/tts";

    private final Context          context;
    private final ExecutorService  executor;
    private final Handler          mainHandler;
    private       MediaPlayer      currentPlayer;
    private final File             cacheDir;

    public QuechuaTTSManager(Context context) {
        this.context     = context.getApplicationContext();
        this.executor    = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cacheDir    = new File(context.getCacheDir(), "tts_cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
    }

    // ──────────────────────────────────────────
    // INTERFAZ CALLBACK
    // ──────────────────────────────────────────
    public interface TTSCallback {
        void onInicio();
        void onFin();
        void onError(String mensaje);
    }

    // ──────────────────────────────────────────
    // MÉTODO PRINCIPAL: reproducir texto
    // ──────────────────────────────────────────
    public void reproducir(String texto, TTSCallback callback) {
        if (texto == null || texto.trim().isEmpty()) {
            if (callback != null) callback.onError("Texto vacío");
            return;
        }

        // Parar reproducción anterior
        detener();

        String textoLimpio = texto.trim();

        // Verificar caché primero
        File cachedFile = archivoCacheParaTexto(textoLimpio);
        if (cachedFile.exists() && cachedFile.length() > 0) {
            Log.d(TAG, "Usando caché: " + cachedFile.getName());
            reproducirDesdeArchivo(cachedFile, callback);
            return;
        }

        // Notificar inicio
        if (callback != null) mainHandler.post(callback::onInicio);

        // Descargar audio en background
        executor.execute(() -> {
            try {
                byte[] audioBytes = descargarAudio(textoLimpio);
                guardarEnCache(cachedFile, audioBytes);

                // Reproducir en main thread
                mainHandler.post(() -> reproducirDesdeArchivo(cachedFile, callback));

            } catch (Exception e) {
                Log.e(TAG, "Error TTS: " + e.getMessage());
                mainHandler.post(() -> {
                    if (callback != null) callback.onError("Sin conexión al servidor TTS");
                });
            }
        });
    }

    // ──────────────────────────────────────────
    // DESCARGAR AUDIO DEL SERVIDOR (base64)
    // ──────────────────────────────────────────
    private byte[] descargarAudio(String texto) throws IOException, JSONException {
        // Usar endpoint b64=1 para recibir JSON con base64
        String urlStr = SERVER_URL
                + "?texto=" + URLEncoder.encode(texto, "UTF-8")
                + "&b64=1";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);  // 10 seg
        conn.setReadTimeout(30_000);     // 30 seg (el modelo puede tardar)
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("HTTP " + code);
        }

        // Leer respuesta
        byte[] responseBytes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            responseBytes = conn.getInputStream().readAllBytes();
        }
        conn.disconnect();

        // Parsear JSON → base64 → bytes
        String responseStr = new String(responseBytes, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(responseStr); // puede lanzar JSONException
        String audioBase64 = json.getString("audio_base64");

        return Base64.decode(audioBase64, Base64.DEFAULT);
    }

    // ──────────────────────────────────────────
    // GUARDAR EN CACHÉ LOCAL
    // ──────────────────────────────────────────
    private void guardarEnCache(File file, byte[] data) {
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(data);
        } catch (IOException e) {
            Log.e(TAG, "Error guardando caché: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────
    // REPRODUCIR DESDE ARCHIVO WAV
    // ──────────────────────────────────────────
    private void reproducirDesdeArchivo(File file, TTSCallback callback) {
        try {
            currentPlayer = new MediaPlayer();
            currentPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            currentPlayer.setDataSource(file.getAbsolutePath());
            currentPlayer.prepareAsync();

            currentPlayer.setOnPreparedListener(mp -> {
                if (callback != null) mainHandler.post(callback::onInicio);
                mp.start();
            });

            currentPlayer.setOnCompletionListener(mp -> {
                mp.release();
                currentPlayer = null;
                if (callback != null) mainHandler.post(callback::onFin);
            });

            currentPlayer.setOnErrorListener((mp, what, extra) -> {
                mp.release();
                currentPlayer = null;
                if (callback != null) mainHandler.post(
                        () -> callback.onError("Error reproduciendo audio")
                );
                return true;
            });

        } catch (Exception e) {
            Log.e(TAG, "Error MediaPlayer: " + e.getMessage());
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    // ──────────────────────────────────────────
    // NOMBRE DE ARCHIVO EN CACHÉ
    // Convierte el texto en un nombre de archivo seguro
    // ──────────────────────────────────────────
    private File archivoCacheParaTexto(String texto) {
        // Hash simple: reemplazar caracteres especiales + limitar largo
        String nombreSeguro = texto.toLowerCase()
                .replaceAll("[^a-z0-9áéíóúüñ']", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (nombreSeguro.length() > 60) {
            nombreSeguro = nombreSeguro.substring(0, 60);
        }

        return new File(cacheDir, nombreSeguro + ".wav");
    }

    // ──────────────────────────────────────────
    // UTILIDADES
    // ──────────────────────────────────────────

    /** Detiene la reproducción actual */
    public void detener() {
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) currentPlayer.stop();
                currentPlayer.release();
            } catch (Exception ignored) {}
            currentPlayer = null;
        }
    }

    /** Limpia toda la caché de audio */
    public void limpiarCache() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        Log.d(TAG, "Caché TTS limpiada");
    }

    /** Cambiar la URL del servidor (útil para configuración dinámica) */
    public static String getServerUrl() { return SERVER_URL; }

    /** Verificar conectividad con el servidor */
    public void verificarServidor(VerificacionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SERVER_URL.replace("/tts", "/ping"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3_000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                conn.disconnect();
                mainHandler.post(() -> callback.onResultado(code == 200, "Servidor disponible"));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResultado(false, "Servidor no disponible: " + e.getMessage()));
            }
        });
    }

    public interface VerificacionCallback {
        void onResultado(boolean disponible, String mensaje);
    }
}