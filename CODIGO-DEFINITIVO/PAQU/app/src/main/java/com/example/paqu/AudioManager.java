package com.example.paqu;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

/**
 * Clase helper para manejar todo el audio de la aplicación
 * Respeta las configuraciones del usuario
 */
public class AudioManager {

    private static AudioManager instance;
    private Context context;
    private SharedPreferences prefs;

    // MediaPlayers reutilizables
    private MediaPlayer musicaFondo;
    private MediaPlayer efectoActual;

    private AudioManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("configuracion_audio", Context.MODE_PRIVATE);
    }

    public static AudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new AudioManager(context);
        }
        return instance;
    }

    // ============= REPRODUCIR EFECTOS =============

    /**
     * Reproduce un efecto de sonido (éxito, error, clic, etc.)
     */
    public void reproducirEfecto(int audioResId) {
        if (!efectosActivados()) return;

        // Detener efecto anterior si existe
        if (efectoActual != null) {
            efectoActual.release();
        }

        try {
            efectoActual = MediaPlayer.create(context, audioResId);
            if (efectoActual != null) {
                float volumen = getVolumenEfectos();
                efectoActual.setVolume(volumen, volumen);
                efectoActual.setOnCompletionListener(mp -> {
                    mp.release();
                    efectoActual = null;
                });
                efectoActual.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Efecto de éxito (respuesta correcta)
     */
    public void reproducirExito() {
        // Reemplaza R.raw.exito con tu archivo de audio
        reproducirEfecto(R.raw.efecto_exito); // Temporal - cambiar después
    }

    /**
     * Efecto de error (respuesta incorrecta)
     */
    public void reproducirError() {
        // Reemplaza R.raw.error con tu archivo de audio
        reproducirEfecto(R.raw.efecto_error); // Temporal - cambiar después
    }

    /**
     * Efecto de clic
     */
    public void reproducirClic() {
        // Reemplaza R.raw.clic con tu archivo de audio
        reproducirEfecto(R.raw.efecto_click); // Temporal - cambiar después
    }

    // ============= MÚSICA DE FONDO =============

    /**
     * Inicia música de fondo en loop
     */
    public void iniciarMusicaFondo(int audioResId) {
        if (!musicaActivada()) return;

        if (musicaFondo != null) {
            musicaFondo.release();
        }

        try {
            musicaFondo = MediaPlayer.create(context, audioResId);
            if (musicaFondo != null) {
                float volumen = getVolumenMusica();
                musicaFondo.setVolume(volumen, volumen);
                musicaFondo.setLooping(true);
                musicaFondo.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pausa la música de fondo
     */
    public void pausarMusicaFondo() {
        if (musicaFondo != null && musicaFondo.isPlaying()) {
            musicaFondo.pause();
        }
    }

    /**
     * Reanuda la música de fondo
     */
    public void reanudarMusicaFondo() {
        if (musicaFondo != null && !musicaFondo.isPlaying() && musicaActivada()) {
            musicaFondo.start();
        }
    }

    /**
     * Detiene y libera la música de fondo
     */
    public void detenerMusicaFondo() {
        if (musicaFondo != null) {
            musicaFondo.stop();
            musicaFondo.release();
            musicaFondo = null;
        }
    }

    // ============= GETTERS DE CONFIGURACIÓN =============

    private boolean musicaActivada() {
        return prefs.getBoolean("musica_activada", true);
    }

    private boolean efectosActivados() {
        return prefs.getBoolean("efectos_activados", true);
    }

    private float getVolumenMusica() {
        int volumen = prefs.getInt("volumen_musica", 70);
        return volumen / 100f;
    }

    private float getVolumenEfectos() {
        int volumen = prefs.getInt("volumen_efectos", 80);
        return volumen / 100f;
    }

    // ============= LIMPIEZA =============

    /**
     * Libera todos los recursos de audio
     */
    public void liberarRecursos() {
        detenerMusicaFondo();
        if (efectoActual != null) {
            efectoActual.release();
            efectoActual = null;
        }
    }
}