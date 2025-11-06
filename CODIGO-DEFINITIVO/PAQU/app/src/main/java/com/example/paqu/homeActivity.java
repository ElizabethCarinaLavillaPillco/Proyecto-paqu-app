package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.paqu.utils.StreakManager;

public class homeActivity extends BaseActivity {

    private CardView stickySection;
    private LinearLayout sectionContent;
    private int stickySectionTop;
    private int originalColor;
    private int stickyColor;

    private StreakManager streakManager;
    private TextView streakDays, diamondsCount, livesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ✅ INICIALIZAR PRIMERO LAS VISTAS
        streakDays = findViewById(R.id.streakDays);
        diamondsCount = findViewById(R.id.diamondsCount);
        livesCount = findViewById(R.id.livesCount);

        // ✅ INICIALIZAR STREAK MANAGER
        streakManager = new StreakManager();

        // Configurar barra de navegación inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Referencias a las vistas sticky
        stickySection = findViewById(R.id.stickySection);
        sectionContent = findViewById(R.id.sectionContent);

        // Obtener colores
        stickyColor = ContextCompat.getColor(this, R.color.rosado);

        // Configurar el efecto sticky con cambio de color
        setupStickyHeader();

        // ✅ PRIMERO ACTUALIZAR RACHA, LUEGO CARGAR DATOS
        updateStreakAndData();

        // Configurar niveles clickeables
        setupLevelCards();

        aplicarFuentesAutomaticas();
    }

    // ✅ MÉTODO UNIFICADO PARA ACTUALIZAR RACHA Y DATOS
    private void updateStreakAndData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Log.d("STREAK_FIX", "🎯 ACTUALIZANDO RACHA PARA: " + userId);

            // 1. ACTUALIZAR RACHA (versión simple)
            streakManager.updateUserStreak(userId, new StreakManager.StreakUpdateCallback() {
                @Override
                public void onStreakUpdated(int newStreak) {
                    Log.d("STREAK_FIX", "🎉 RACHA ACTUALIZADA: " + newStreak);

                    runOnUiThread(() -> {
                        streakDays.setText(String.valueOf(newStreak));
                        Toast.makeText(homeActivity.this, "🔥 Racha: " + newStreak + " días", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e("STREAK_FIX", "💥 ERROR: " + error);

                    runOnUiThread(() -> {
                        // SI HAY ERROR, mostrar 1 igualmente
                        streakDays.setText("1");
                        Toast.makeText(homeActivity.this, "Racha iniciada: 1 día", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            // 2. CARGAR DATOS EXISTENTES (si los tienes)
            loadExistingUserData(userId);

        } else {
            Log.e("STREAK_FIX", "❌ Usuario no logueado");
            streakDays.setText("1");
        }
    }

    // ✅ CARGAR DATOS EXISTENTES (OPCIONAL)
    private void loadExistingUserData(String userId) {
        // Si tienes datos en otra ubicación, cargarlos aquí
        // Por ahora, poner valores por defecto
        runOnUiThread(() -> {
            diamondsCount.setText("0");
            livesCount.setText("5");
        });
    }
    // ✅ MÉTODO PARA CARGAR DIAMANTES Y VIDAS
    private void loadUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long diamantes = snapshot.child("diamantes").getValue(Long.class);
                    Long vidas = snapshot.child("vidas").getValue(Long.class);

                    runOnUiThread(() -> {
                        diamondsCount.setText(diamantes != null ? String.valueOf(diamantes) : "0");
                        livesCount.setText(vidas != null ? String.valueOf(vidas) : "0");
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DATA_DEBUG", "Error datos: " + error.getMessage());
            }
        });
    }

    // MÉTODO NUEVO - VERSIÓN SIMPLE Y EFECTIVA
    private void aplicarFuentesAutomaticas() {
        // Header
        aplicarFuente(R.id.streakDays, 16f);
        aplicarFuente(R.id.diamondsCount, 16f);
        aplicarFuente(R.id.livesCount, 16f);

        // Sección sticky
        CardView stickyCard = findViewById(R.id.stickySection);
        if (stickyCard != null) {
            LinearLayout content = findViewById(R.id.sectionContent);
            if (content != null && content.getChildCount() >= 2) {
                View child1 = content.getChildAt(0);
                View child2 = content.getChildAt(1);

                if (child1 instanceof TextView)
                    configuracionActivity.aplicarTamanioFuente((TextView) child1, 20f);
                if (child2 instanceof TextView)
                    configuracionActivity.aplicarTamanioFuente((TextView) child2, 16f);
            }
        }

        // Niveles
        aplicarFuentesNiveles();
    }

    private void aplicarFuentesNiveles() {
        int[] levelIds = {R.id.level1Card, R.id.level2Card, R.id.level3Card,
                R.id.level4Card, R.id.level5Card, R.id.level6Card};

        for (int id : levelIds) {
            View levelView = findViewById(id);
            if (levelView != null) {
                TextView title = levelView.findViewById(R.id.levelTitle);
                TextView desc = levelView.findViewById(R.id.levelDescription);

                if (title != null) configuracionActivity.aplicarTamanioFuente(title, 16f);
                if (desc != null) configuracionActivity.aplicarTamanioFuente(desc, 14f);
            }
        }
    }

    private void aplicarFuente(int textViewId, float tamanioBase) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            configuracionActivity.aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    private void setupStickyHeader() {
        final ViewTreeObserver observer = stickySection.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                stickySectionTop = stickySection.getTop();
                stickySection.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                findViewById(R.id.mainScrollView).getViewTreeObserver()
                        .addOnScrollChangedListener(() -> {
                            int scrollY = findViewById(R.id.mainScrollView).getScrollY();
                            if (scrollY >= stickySectionTop) {
                                sectionContent.setBackgroundColor(stickyColor);
                            } else {
                                stickySection.setTranslationY(0);
                                sectionContent.setBackgroundColor(originalColor);
                            }
                        });
            }
        });
    }

    private void setupLevelCards() {
        int[] levelCardIds = {R.id.level1Card, R.id.level2Card, R.id.level3Card,
                R.id.level4Card, R.id.level5Card, R.id.level6Card};

        String[] descriptions = {
                "Saludos básicos",
                "Presentaciones",
                "Familia",
                "Números",
                "Colores",
                "Comida"
        };

        SharedPreferences prefs = getSharedPreferences("niveles", MODE_PRIVATE);
        int totalCompletados = 0;

        for (int i = 0; i < levelCardIds.length; i++) {
            CardView levelCard = findViewById(levelCardIds[i]);
            if (levelCard == null) continue;

            final int levelNumber = i + 1;
            boolean completado = prefs.getBoolean("nivel" + levelNumber, false);

            TextView levelTitle = levelCard.findViewById(R.id.levelTitle);
            TextView levelDesc = levelCard.findViewById(R.id.levelDescription);
            ProgressBar progressBar = levelCard.findViewById(R.id.levelProgress);
            ImageView statusIcon = levelCard.findViewById(R.id.levelStatusIcon);

            levelTitle.setText(String.format("Nivel %d", levelNumber));
            levelDesc.setText(descriptions[i]);

            if (completado) {
                progressBar.setProgress(100);
                progressBar.setAlpha(1f);
                progressBar.getProgressDrawable().setColorFilter(
                        ContextCompat.getColor(this, R.color.morado),
                        PorterDuff.Mode.SRC_IN);
                statusIcon.setImageResource(R.drawable.ic_check);
                totalCompletados++;
            } else {
                progressBar.setProgress(0);
                progressBar.setAlpha(0.5f);
                progressBar.getProgressDrawable().setColorFilter(
                        ContextCompat.getColor(this, R.color.grey),
                        PorterDuff.Mode.SRC_IN);
                statusIcon.setImageResource(R.drawable.ic_cross);
            }

            levelCard.setOnClickListener(v -> {
                navigateToExercise(levelNumber);
            });
        }

        ProgressBar sectionProgressBar = findViewById(R.id.sectionProgressBar);
        if (sectionProgressBar != null) {
            float progreso = (float) totalCompletados / 6f;
            int porcentaje = Math.round(progreso * 100);

            sectionProgressBar.setProgress(porcentaje);

            if (porcentaje == 0) {
                sectionProgressBar.getProgressDrawable().setColorFilter(
                        ContextCompat.getColor(this, R.color.grey), PorterDuff.Mode.SRC_IN);
            } else {
                sectionProgressBar.getProgressDrawable().setColorFilter(
                        ContextCompat.getColor(this, R.color.rosado), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void navigateToExercise(int levelNumber) {
        try {
            if (levelNumber == 1) {
                Intent intent = new Intent(this, ejercicio1.class);
                intent.putExtra("LEVEL_NUMBER", levelNumber);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Nivel " + levelNumber + " en desarrollo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_home) {
                        return true;
                    } else if (id == R.id.nav_dictionary) {
                        startActivity(new Intent(homeActivity.this, diccionarioActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_minijuegos) {
                        startActivity(new Intent(homeActivity.this, MiniJuegosActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(homeActivity.this, perfilActivity.class));
                        finish();
                        return true;
                    }
                    return false;
                }
            };

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ ACTUALIZAR RACHA Y DATOS CADA VEZ QUE SE ABRE LA APP
        updateStreakAndData();
        setupLevelCards();
        aplicarFuentesAutomaticas();
    }
}