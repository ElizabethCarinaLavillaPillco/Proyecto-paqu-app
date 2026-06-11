package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class LeccionAcabadaActivity extends AppCompatActivity {

    TextView tvExpGanado, tvTiempo, tvRacha;
    Button btnTermin;

    long vidasRestantes = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leccion_acabada);
        // ===== MARCAR NIVEL 1 COMO COMPLETADO =====
        SharedPreferences prefs =
                getSharedPreferences("game_data", MODE_PRIVATE);

        int nivelActual = 1;

        prefs.edit()
                .putBoolean("nivel1", true)
                .putInt("nivel_completado", nivelActual + 1)
                .apply();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular UI
        tvExpGanado = findViewById(R.id.tvExpGanado);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvRacha = findViewById(R.id.tvRacha);
        btnTermin = findViewById(R.id.btnTermin);

        // Desempaquetar parámetros del Intent
        int exp = getIntent().getIntExtra("exp", 0);
        long tiempoMs = getIntent().getLongExtra("tiempo", 0);
        vidasRestantes = getIntent().getLongExtra("vidas", 5);

        // Formatear el tiempo de sesión de manera amigable
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        segundos = segundos % 60;

        String tiempoFormato;
        if (minutos > 0) {
            tiempoFormato = minutos + " min " + segundos + " seg";
        } else {
            tiempoFormato = segundos + " seg";
        }

        // Asignar métricas visuales
        tvExpGanado.setText("+" + exp + " XP");
        tvTiempo.setText(tiempoFormato);

        // Actualizar estados asíncronos y persistencia
        actualizarRacha(tvRacha);
        actualizarVidasFirebase(vidasRestantes);

        // Configurar acción del botón final
        btnTermin.setOnClickListener(v -> {
            guardarProgreso(1); // Registrar nivel 1 resuelto con éxito

            Intent intent = new Intent(LeccionAcabadaActivity.this, homeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Limpiar rastro de la actividad
        });
    }

    private void actualizarRacha(TextView tvRacha) {
        SharedPreferences prefs = getSharedPreferences("rachas", MODE_PRIVATE);
        long ultimaEntrada = prefs.getLong("ultimaEntrada", 0);
        int rachaActual = prefs.getInt("rachaActual", 0);

        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        long hoyMillis = hoy.getTimeInMillis();

        // Evaluar condiciones de la racha temporal
        if (ultimaEntrada == hoyMillis - 86400000L) {
            rachaActual++; // Secuencia consecutiva perfecta
        } else if (ultimaEntrada != hoyMillis) {
            rachaActual = 1; // Racha rota o inicial
        }

        prefs.edit()
                .putLong("ultimaEntrada", hoyMillis)
                .putInt("rachaActual", rachaActual)
                .apply();

        tvRacha.setText(String.valueOf(rachaActual));

        // Actualización remota en Firebase Realtime Database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Usuarios")
                    .child(user.getUid());

            userRef.child("racha").setValue(rachaActual);
            userRef.child("ultimaFecha").setValue(hoyMillis);
        }
    }

    private void actualizarVidasFirebase(long vidasRestantes) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Usuarios")
                    .child(user.getUid());

            ref.child("vidas").setValue(vidasRestantes);
        }
    }

    private void guardarProgreso(int nivelCompletado) {
        SharedPreferences prefs = getSharedPreferences("niveles", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("nivel" + nivelCompletado, true);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        // Bloquear acción del botón físico atrás para forzar el uso del botón de flujo "Continuar"
        super.onBackPressed();
    }
}