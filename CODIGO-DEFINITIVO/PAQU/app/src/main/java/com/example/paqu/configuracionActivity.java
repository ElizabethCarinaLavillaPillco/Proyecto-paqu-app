package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class configuracionActivity extends AppCompatActivity {

    Button btnPreferencias, btnPerfil, btnNotificaciones, btnAPrivacidad,
            btnCentroAyuda, btnSugerencias, btnTamanioFuente, btnVolumen;
    TextView tvOk, tvPoliticaPrivacidad, tvTerminos, tvCerrarSesion;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Aplicar fuentes automáticamente
        aplicarFuentesAutomaticas();

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        initViews();

        // Aplicar animaciones de entrada
        aplicarAnimacionesEntrada();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        // Botones
        btnPreferencias = findViewById(R.id.btnPreferencias);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        btnAPrivacidad = findViewById(R.id.btnAPrivacidad);
        btnCentroAyuda = findViewById(R.id.btnCentroAyuda);
        btnSugerencias = findViewById(R.id.btnSugerencias);
        btnTamanioFuente = findViewById(R.id.btnTamanioFuente);
        btnVolumen = findViewById(R.id.btnVolumen);

        // TextViews
        tvOk = findViewById(R.id.tvOk);
        tvCerrarSesion = findViewById(R.id.tvCerrarSesion);
        tvTerminos = findViewById(R.id.tvTerminos);
        tvPoliticaPrivacidad = findViewById(R.id.tvPoliticaPrivacidad);
    }

    private void setupListeners() {
        // OK - Volver
        tvOk.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, perfilActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });

        // Cerrar Sesión
        tvCerrarSesion.setOnClickListener(v -> {
            animarClick(v);
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Tamaño de Fuente
        btnTamanioFuente.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, TamanioFuenteActivity.class));
        });

        // 🔊 NUEVO: Botón Volumen
        btnVolumen.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, VolumenAjusteActivity.class));
        });

        // Perfil
        btnPerfil.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, perfilActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });

        // Resto de botones (placeholder)
        btnPreferencias.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Preferencias", Toast.LENGTH_SHORT).show();
        });

        btnNotificaciones.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
        });

        btnAPrivacidad.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Ajustes de Privacidad", Toast.LENGTH_SHORT).show();
        });

        btnCentroAyuda.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Centro de Ayuda", Toast.LENGTH_SHORT).show();
        });

        btnSugerencias.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Sugerencias", Toast.LENGTH_SHORT).show();
        });

        tvPoliticaPrivacidad.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Política de Privacidad", Toast.LENGTH_SHORT).show();
        });

        tvTerminos.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Términos y Condiciones", Toast.LENGTH_SHORT).show();
        });
    }

    // ============= ANIMACIONES =============

    private void aplicarAnimacionesEntrada() {
        Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideIn.setDuration(400);

        View[] vistas = {
                btnPreferencias, btnPerfil, btnNotificaciones,
                btnTamanioFuente, btnVolumen,
                btnAPrivacidad, btnCentroAyuda, btnSugerencias
        };

        for (int i = 0; i < vistas.length; i++) {
            View vista = vistas[i];
            vista.setAlpha(0f);
            vista.setTranslationX(-100f);

            vista.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(500)
                    .setStartDelay(i * 80)
                    .start();
        }
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    // ============= FUENTES =============

    private void aplicarFuentesAutomaticas() {
        aplicarFuente(R.id.textView17, 20f);
        aplicarFuente(R.id.textView18, 16f);
        aplicarFuente(R.id.text, 16f);
        aplicarFuente(R.id.text5, 16f);
        aplicarFuente(R.id.tvOk, 16f);
        aplicarFuente(R.id.tvCerrarSesion, 16f);
        aplicarFuente(R.id.tvPoliticaPrivacidad, 14f);
        aplicarFuente(R.id.tvTerminos, 14f);
        aplicarFuenteBotones();
    }

    private void aplicarFuenteBotones() {
        int[] botonesIds = {
                R.id.btnPreferencias, R.id.btnPerfil, R.id.btnNotificaciones,
                R.id.btnAPrivacidad, R.id.btnCentroAyuda, R.id.btnSugerencias,
                R.id.btnTamanioFuente, R.id.btnVolumen
        };

        for (int botonId : botonesIds) {
            Button boton = findViewById(botonId);
            if (boton != null) {
                aplicarTamanioFuente(boton, 14f);
            }
        }
    }

    private void aplicarFuente(int textViewId, float tamanioBase) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarFuentesAutomaticas();
    }

    // Métodos estáticos (para uso global)
    public static float obtenerFactorFuente(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("configuracion_app", android.content.Context.MODE_PRIVATE);
        return prefs.getFloat("factor_fuente", 1.0f);
    }

    public static void aplicarTamanioFuente(TextView textView, float tamanioBase) {
        float factor = obtenerFactorFuente(textView.getContext());
        float nuevoTamanio = tamanioBase * factor;
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, nuevoTamanio);
    }
}