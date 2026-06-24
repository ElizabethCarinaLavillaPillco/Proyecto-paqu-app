package com.example.paqu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Dashboard principal para el rol DOCENTE.
 * Acceso a: crear lecciones, gestionar palabras del diccionario,
 * ver mis lecciones y estadísticas de estudiantes.
 */
public class DocenteHomeActivity extends AppCompatActivity {

    private static final String TAG = "DocenteHome";

    // Header
    private TextView tvDocenteNombre;
    private TextView tvDocenteEmail;
    private TextView tvTotalLecciones;
    private TextView tvTotalPalabras;
    private TextView tvTotalEstudiantes;
    private ImageView ivAvatar;

    // Cards de acción
    private CardView cardCrearLeccion;
    private CardView cardMisLecciones;
    private CardView cardGestionPalabras;
    private CardView cardEstadisticas;
    private CardView cardTraduttore;

    // Animaciones
    private View[] animatedCards;

    private DatabaseReference dbRef;
    private FirebaseUser currentUser;
    private String docenteUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docente_home);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, loginFormActivity.class));
            finish();
            return;
        }
        docenteUid = currentUser.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        cargarDatosDocente();
        setupClickListeners();
        animarEntrada();
    }

    // ─────────────────────────────────────────────
    // INICIALIZAR VISTAS
    // ─────────────────────────────────────────────
    private void initViews() {
        tvDocenteNombre     = findViewById(R.id.tvDocenteNombre);
        tvDocenteEmail      = findViewById(R.id.tvDocenteEmail);
        tvTotalLecciones    = findViewById(R.id.tvTotalLecciones);
        tvTotalPalabras     = findViewById(R.id.tvTotalPalabras);
        tvTotalEstudiantes  = findViewById(R.id.tvTotalEstudiantes);
        ivAvatar            = findViewById(R.id.ivAvatarDocente);

        cardCrearLeccion    = findViewById(R.id.cardCrearLeccion);
        cardMisLecciones    = findViewById(R.id.cardMisLecciones);
        cardGestionPalabras = findViewById(R.id.cardGestionPalabras);
        cardEstadisticas    = findViewById(R.id.cardEstadisticasDocente);
        cardTraduttore      = findViewById(R.id.cardTraduttore);

        animatedCards = new View[]{
            cardCrearLeccion, cardMisLecciones,
            cardGestionPalabras, cardEstadisticas, cardTraduttore
        };
    }

    // ─────────────────────────────────────────────
    // CARGAR DATOS DEL DOCENTE DESDE FIREBASE
    // ─────────────────────────────────────────────
    private void cargarDatosDocente() {
        // Datos del usuario
        dbRef.child("users").child(docenteUid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nombre = snapshot.child("name").getValue(String.class);
                    String email  = snapshot.child("email").getValue(String.class);

                    if (nombre == null) nombre = "Docente";
                    if (email  == null) email  = currentUser.getEmail();

                    tvDocenteNombre.setText("¡Hola, " + nombre + "! 👋");
                    tvDocenteEmail.setText(email);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error usuario: " + error.getMessage());
                }
            });

        // Contar lecciones del docente
        dbRef.child("lessons")
            .orderByChild("lessonInfo/createdBy")
            .equalTo(docenteUid)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    animarContador(tvTotalLecciones, count);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        // Contar palabras del diccionario creadas por este docente
        dbRef.child("diccionario")
            .orderByChild("creadoPor")
            .equalTo(docenteUid)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    animarContador(tvTotalPalabras, count);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        // Contar estudiantes totales (rol usuario_comun)
        dbRef.child("users")
            .orderByChild("role")
            .equalTo("usuario_comun")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    animarContador(tvTotalEstudiantes, count);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    // ─────────────────────────────────────────────
    // CLICK LISTENERS
    // ─────────────────────────────────────────────
    private void setupClickListeners() {

        // Crear nueva lección
        cardCrearLeccion.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() -> startActivity(
                new Intent(this, CrearLeccionActivity.class)
            ), 150);
        });

        // Ver mis lecciones
        cardMisLecciones.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() -> startActivity(
                new Intent(this, MisLeccionesActivity.class)
            ), 150);
        });

        // Gestionar palabras del diccionario
        cardGestionPalabras.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() -> startActivity(
                new Intent(this, GestionPalabrasDocenteActivity.class)
            ), 150);
        });

        // Estadísticas de estudiantes
        cardEstadisticas.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Estadísticas próximamente 📊", Toast.LENGTH_SHORT).show();
        });

        // Cerrar sesión
        ImageView btnLogout = findViewById(R.id.btnLogoutDocente);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().clear().apply();
                Intent intent = new Intent(this, loginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    // ─────────────────────────────────────────────
    // ANIMACIONES
    // ─────────────────────────────────────────────
    private void animarEntrada() {
        // Header: slide down
        View header = findViewById(R.id.headerDocente);
        if (header != null) {
            header.setTranslationY(-200f);
            header.setAlpha(0f);
            header.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        }

        // Cards: cascada con bounce
        for (int i = 0; i < animatedCards.length; i++) {
            View card = animatedCards[i];
            card.setAlpha(0f);
            card.setTranslationY(80f);
            card.setScaleX(0.85f);
            card.setScaleY(0.85f);

            card.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(200 + i * 100L)
                .setInterpolator(new BounceInterpolator())
                .start();
        }
    }

    private void animarClick(View v) {
        v.animate()
            .scaleX(0.93f).scaleY(0.93f)
            .setDuration(100)
            .withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            ).start();
    }

    private void animarContador(TextView tv, long valor) {
        runOnUiThread(() -> {
            // Simple contador animado de 0 → valor
            final long[] current = {0};
            final long step = Math.max(1, valor / 20);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    current[0] = Math.min(current[0] + step, valor);
                    tv.setText(String.valueOf(current[0]));
                    if (current[0] < valor) {
                        tv.postDelayed(this, 50);
                    }
                }
            };
            tv.post(runnable);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosDocente();
    }
}