package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class configuracionActivity extends AppCompatActivity {

    Button btnPreferencias, btnPerfil, btnNotificaciones, btnAPrivacidad, btnCentroAyuda, btnSugerencias, btnTamanioFuente;
    TextView tvOk, tvPoliticaPrivacidad, tvTerminos, tvCerrarSesion;

    FirebaseAuth mAuth;
    Button btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // NUEVO: Aplicar fuentes automáticamente
        aplicarFuentesAutomaticas();

        // botones
        btnPreferencias = findViewById(R.id.btnPreferencias);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        btnAPrivacidad = findViewById(R.id.btnAPrivacidad);
        btnCentroAyuda = findViewById(R.id.btnCentroAyuda);
        btnSugerencias = findViewById(R.id.btnSugerencias);
        btnTamanioFuente = findViewById(R.id.btnTamanioFuente);

        // textos que actuan como botones
        tvOk = findViewById(R.id.tvOk);
        tvCerrarSesion = findViewById(R.id.tvCerrarSesion);
        tvTerminos = findViewById(R.id.tvTerminos);
        tvPoliticaPrivacidad = findViewById(R.id.tvPoliticaPrivacidad);

        // acciones
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(configuracionActivity.this, perfilActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });

        // Cerrar sesión
        mAuth = FirebaseAuth.getInstance();
        tvCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // Cierra la sesión
                Intent intent = new Intent(configuracionActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Finaliza esta actividad
            }
        });

        // Botón de tamaño de fuente
        btnTamanioFuente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(configuracionActivity.this, TamanioFuenteActivity.class);
                startActivity(intent);
            }
        });

        // Acciones para los otros botones
        btnPreferencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Preferencias", Toast.LENGTH_SHORT).show();
            }
        });

        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(configuracionActivity.this, perfilActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });

        btnNotificaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Notificaciones", Toast.LENGTH_SHORT).show();
            }
        });

        btnAPrivacidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Ajustes de Privacidad", Toast.LENGTH_SHORT).show();
            }
        });

        btnCentroAyuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Centro de Ayuda", Toast.LENGTH_SHORT).show();
            }
        });

        btnSugerencias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Sugerencias", Toast.LENGTH_SHORT).show();
            }
        });

        // Acciones para los textos de política y términos
        tvPoliticaPrivacidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Política de Privacidad", Toast.LENGTH_SHORT).show();
            }
        });

        tvTerminos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(configuracionActivity.this, "Términos y Condiciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // NUEVO: Método para aplicar fuentes a TODOS los TextView
    private void aplicarFuentesAutomaticas() {
        // TextView principales
        aplicarFuente(R.id.textView17, 20f);  // "Configuración"
        aplicarFuente(R.id.textView18, 16f);  // "Cuenta"
        aplicarFuente(R.id.text, 16f);        // "Accesibilidad"
        aplicarFuente(R.id.text5, 16f);       // "Soporte"

        // TextView que actúan como botones
        aplicarFuente(R.id.tvOk, 16f);        // "OK"
        aplicarFuente(R.id.tvCerrarSesion, 16f); // "Cerrar Sesión"
        aplicarFuente(R.id.tvPoliticaPrivacidad, 14f); // "POLÍTICA DE PRIVACIDAD"
        aplicarFuente(R.id.tvTerminos, 14f);  // "TÉRMINOS"

        // Aplicar también a los textos de los botones
        aplicarFuenteBotones();
    }

    // NUEVO: Método para aplicar fuentes a los botones
    private void aplicarFuenteBotones() {
        int[] botonesIds = {
                R.id.btnPreferencias, R.id.btnPerfil, R.id.btnNotificaciones,
                R.id.btnAPrivacidad, R.id.btnCentroAyuda, R.id.btnSugerencias,
                R.id.btnTamanioFuente
        };

        for (int botonId : botonesIds) {
            Button boton = findViewById(botonId);
            if (boton != null) {
                aplicarTamanioFuente(boton, 14f);
            }
        }
    }

    // NUEVO: Método helper
    private void aplicarFuente(int textViewId, float tamanioBase) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // NUEVO: Re-aplicar fuentes por si hubo cambios
        aplicarFuentesAutomaticas();
    }

    // MÉTODOS ESTÁTICOS (ya existían)
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