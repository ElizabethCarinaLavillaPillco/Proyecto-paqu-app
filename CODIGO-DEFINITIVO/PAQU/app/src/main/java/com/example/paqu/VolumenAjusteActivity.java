package com.example.paqu;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class VolumenAjusteActivity extends AppCompatActivity {

    // UI Components
    SwitchCompat switchMusica, switchEfectos;
    SeekBar seekBarMusica, seekBarEfectos;
    TextView tvVolumenMusica, tvVolumenEfectos, tvTitulo;
    Button btnGuardar, btnCancelar, btnProbarMusica, btnProbarEfecto;
    ImageView iconMusica, iconEfectos, iconAtras;

    // MediaPlayers para pruebas
    MediaPlayer musicaEjemplo, efectoEjemplo;

    // SharedPreferences
    SharedPreferences prefs;

    // Firebase
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    // Valores actuales
    boolean musicaActivada = true;
    boolean efectosActivados = true;
    int volumenMusica = 70;
    int volumenEfectos = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volumen_ajuste);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // Inicializar SharedPreferences
        prefs = getSharedPreferences("configuracion_audio", MODE_PRIVATE);

        // Inicializar vistas
        initViews();

        // Cargar configuración guardada
        cargarConfiguracion();

        // Configurar listeners
        setupListeners();

        // Animaciones de entrada
        animarEntrada();
    }

    private void initViews() {
        // Switches
        switchMusica = findViewById(R.id.switchMusica);
        switchEfectos = findViewById(R.id.switchEfectos);

        // SeekBars
        seekBarMusica = findViewById(R.id.seekBarMusica);
        seekBarEfectos = findViewById(R.id.seekBarEfectos);

        // TextViews
        tvVolumenMusica = findViewById(R.id.tvVolumenMusica);
        tvVolumenEfectos = findViewById(R.id.tvVolumenEfectos);
        tvTitulo = findViewById(R.id.tvTitulo);

        // Botones
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnProbarMusica = findViewById(R.id.btnProbarMusica);
        btnProbarEfecto = findViewById(R.id.btnProbarEfecto);

        // ImageViews
        iconMusica = findViewById(R.id.iconMusica);
        iconEfectos = findViewById(R.id.iconEfectos);
        iconAtras = findViewById(R.id.iconAtras);
    }

    private void cargarConfiguracion() {
        // Cargar desde SharedPreferences
        musicaActivada = prefs.getBoolean("musica_activada", true);
        efectosActivados = prefs.getBoolean("efectos_activados", true);
        volumenMusica = prefs.getInt("volumen_musica", 70);
        volumenEfectos = prefs.getInt("volumen_efectos", 80);

        // Aplicar a la UI
        switchMusica.setChecked(musicaActivada);
        switchEfectos.setChecked(efectosActivados);
        seekBarMusica.setProgress(volumenMusica);
        seekBarEfectos.setProgress(volumenEfectos);

        tvVolumenMusica.setText(volumenMusica + "%");
        tvVolumenEfectos.setText(volumenEfectos + "%");

        // Habilitar/deshabilitar SeekBars según switches
        seekBarMusica.setEnabled(musicaActivada);
        seekBarEfectos.setEnabled(efectosActivados);
        btnProbarMusica.setEnabled(musicaActivada);
        btnProbarEfecto.setEnabled(efectosActivados);

        // Cambiar opacidad de iconos
        iconMusica.setAlpha(musicaActivada ? 1.0f : 0.4f);
        iconEfectos.setAlpha(efectosActivados ? 1.0f : 0.4f);
    }

    private void setupListeners() {
        // Switch Música
        switchMusica.setOnCheckedChangeListener((buttonView, isChecked) -> {
            musicaActivada = isChecked;
            seekBarMusica.setEnabled(isChecked);
            btnProbarMusica.setEnabled(isChecked);
            animarIcono(iconMusica, isChecked);

            if (!isChecked && musicaEjemplo != null && musicaEjemplo.isPlaying()) {
                musicaEjemplo.pause();
            }
        });

        // Switch Efectos
        switchEfectos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            efectosActivados = isChecked;
            seekBarEfectos.setEnabled(isChecked);
            btnProbarEfecto.setEnabled(isChecked);
            animarIcono(iconEfectos, isChecked);

            if (!isChecked && efectoEjemplo != null && efectoEjemplo.isPlaying()) {
                efectoEjemplo.pause();
            }
        });

        // SeekBar Música
        seekBarMusica.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumenMusica = progress;
                tvVolumenMusica.setText(progress + "%");
                if (musicaEjemplo != null && musicaEjemplo.isPlaying()) {
                    float volumen = progress / 100f;
                    musicaEjemplo.setVolume(volumen, volumen);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animarTexto(tvVolumenMusica);
            }
        });

        // SeekBar Efectos
        seekBarEfectos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumenEfectos = progress;
                tvVolumenEfectos.setText(progress + "%");
                if (efectoEjemplo != null && efectoEjemplo.isPlaying()) {
                    float volumen = progress / 100f;
                    efectoEjemplo.setVolume(volumen, volumen);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animarTexto(tvVolumenEfectos);
            }
        });

        // Botón Probar Música
        btnProbarMusica.setOnClickListener(v -> {
            probarAudio(true);
        });

        // Botón Probar Efecto
        btnProbarEfecto.setOnClickListener(v -> {
            probarAudio(false);
        });

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> {
            guardarConfiguracion();
        });

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> {
            finish();
        });

        // Icono Atrás
        iconAtras.setOnClickListener(v -> {
            finish();
        });
    }

    private void probarAudio(boolean esMusica) {
        if (esMusica) {
            // Detener efecto si está sonando
            if (efectoEjemplo != null && efectoEjemplo.isPlaying()) {
                efectoEjemplo.pause();
            }

            // Reproducir música de ejemplo
            if (musicaEjemplo != null) {
                musicaEjemplo.release();
            }

            // Usa un audio de fondo que tengas (ej: R.raw.musica_fondo)
            // Si no tienes, comenta esta línea
            musicaEjemplo = MediaPlayer.create(this, R.raw.voz_buenos_dias); // Temporal

            if (musicaEjemplo != null) {
                float volumen = volumenMusica / 100f;
                musicaEjemplo.setVolume(volumen, volumen);
                musicaEjemplo.start();
                animarBoton(btnProbarMusica);
                Toast.makeText(this, "🎵 Reproduciendo música de ejemplo", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Detener música si está sonando
            if (musicaEjemplo != null && musicaEjemplo.isPlaying()) {
                musicaEjemplo.pause();
            }

            // Reproducir efecto de ejemplo
            if (efectoEjemplo != null) {
                efectoEjemplo.release();
            }

            // Usa un efecto de sonido que tengas
            efectoEjemplo = MediaPlayer.create(this, R.raw.voz_samay); // Temporal

            if (efectoEjemplo != null) {
                float volumen = volumenEfectos / 100f;
                efectoEjemplo.setVolume(volumen, volumen);
                efectoEjemplo.start();
                animarBoton(btnProbarEfecto);
                Toast.makeText(this, "🔔 Reproduciendo efecto de ejemplo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarConfiguracion() {
        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("musica_activada", musicaActivada);
        editor.putBoolean("efectos_activados", efectosActivados);
        editor.putInt("volumen_musica", volumenMusica);
        editor.putInt("volumen_efectos", volumenEfectos);
        editor.apply();

        // Guardar en Firebase si el usuario está autenticado
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Map<String, Object> audioConfig = new HashMap<>();
            audioConfig.put("musica_activada", musicaActivada);
            audioConfig.put("efectos_activados", efectosActivados);
            audioConfig.put("volumen_musica", volumenMusica);
            audioConfig.put("volumen_efectos", volumenEfectos);

            dbRef.child(userId).child("configuracion_audio").setValue(audioConfig)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();
                        animarBoton(btnGuardar);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ Error al guardar en servidor", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "✅ Configuración guardada localmente", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ============= ANIMACIONES =============

    private void animarEntrada() {
        View[] vistas = {tvTitulo, switchMusica, seekBarMusica, switchEfectos, seekBarEfectos,
                btnProbarMusica, btnProbarEfecto, btnGuardar, btnCancelar};

        for (int i = 0; i < vistas.length; i++) {
            View vista = vistas[i];
            vista.setAlpha(0f);
            vista.setTranslationY(50f);

            vista.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(i * 50)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void animarIcono(ImageView icono, boolean activado) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(icono, "alpha",
                activado ? 0.4f : 1.0f,
                activado ? 1.0f : 0.4f);
        animator.setDuration(300);
        animator.start();

        // Animación de escala
        icono.animate()
                .scaleX(activado ? 1.1f : 0.9f)
                .scaleY(activado ? 1.1f : 0.9f)
                .setDuration(200)
                .withEndAction(() -> {
                    icono.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                })
                .start();
    }

    private void animarTexto(TextView tv) {
        tv.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() -> {
                    tv.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                })
                .start();
    }

    private void animarBoton(Button btn) {
        btn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> {
                    btn.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        // Liberar recursos de audio
        if (musicaEjemplo != null) {
            musicaEjemplo.release();
            musicaEjemplo = null;
        }
        if (efectoEjemplo != null) {
            efectoEjemplo.release();
            efectoEjemplo = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar audios al salir
        if (musicaEjemplo != null && musicaEjemplo.isPlaying()) {
            musicaEjemplo.pause();
        }
        if (efectoEjemplo != null && efectoEjemplo.isPlaying()) {
            efectoEjemplo.pause();
        }
    }
}