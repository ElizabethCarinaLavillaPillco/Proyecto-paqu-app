package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import androidx.annotation.NonNull;
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
    private CardView cardMapaVariantes;
    private ImageView ivMapaQuechua;
    private CardView stickySection;
    private LinearLayout sectionContent;
    private int stickySectionTop;
    private int originalColor;
    private int stickyColor;
    private CardView draggableBubble;
    private CardView curiositiesBubble;
    private float dX, dY;
    private float dX2, dY2;
    private int lastAction;
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

        SharedPreferences prefs =
                getSharedPreferences("game_data", MODE_PRIVATE);

        if (!prefs.contains("vidas")) {
            prefs.edit()
                    .putLong("vidas", 5)
                    .apply();
        }

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
        setupMapaButton();

        aplicarFuentesAutomaticas();

        // ✅ INICIALIZAR BURBUJA DE REPASO
        draggableBubble = findViewById(R.id.draggableBubble);
        setupDraggableBubble(draggableBubble, true);

        // ✅ INICIALIZAR BURBUJA DE DATOS CURIOSOS
        curiositiesBubble = findViewById(R.id.curiositiesBubble);
        setupDraggableBubble(curiositiesBubble, false);

        mostrarToastBienvenida();
    }

    private void mostrarToastBienvenida() {
        // Obtener el rol del Intent
        String rol = getIntent().getStringExtra("user_role");

        // Si no viene del Intent, obtener de SharedPreferences
        if (rol == null || rol.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            rol = prefs.getString("user_role", "usuario_comun");
        }

        // Formatear el mensaje según el rol
        String mensaje;
        int colorToast;

        switch (rol) {
            case "administrador":
                mensaje = "👑 Bienvenido, Administrador";
                colorToast = android.R.color.holo_red_light;
                break;
            case "docente":
                mensaje = "📚 Bienvenido, Docente";
                colorToast = android.R.color.holo_blue_light;
                break;
            case "usuario_comun":
            default:
                mensaje = "👋 Bienvenido, Aprendiz";
                colorToast = android.R.color.holo_green_light;
                break;
        }

        // Mostrar Toast personalizado
        Toast toast = Toast.makeText(this, mensaje, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();

        Log.d("ROL_LOGIN", "Usuario ingresó como: " + rol);
    }

    private void setupMapaButton() {
        cardMapaVariantes = findViewById(R.id.cardMapaVariantes);
        ivMapaQuechua = findViewById(R.id.ivMapaQuechua);

        // Hacer clickeable el CardView completo
        if (cardMapaVariantes != null) {
            cardMapaVariantes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirMapaVariantes();
                }
            });
        }

        // También hacer clickeable el ImageView
        if (ivMapaQuechua != null) {
            ivMapaQuechua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirMapaVariantes();
                }
            });
        }
    }

    private void abrirMapaVariantes() {
        try {
            // Animación de feedback visual
            if (cardMapaVariantes != null) {
                cardMapaVariantes.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                cardMapaVariantes.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100)
                                        .start();
                            }
                        })
                        .start();
            }

            // Abrir MapaVariantesActivity
            Intent intent = new Intent(homeActivity.this, MapaVariantesActivity.class);
            startActivity(intent);

            // Animación de transición suave
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e("MAPA_ERROR", "Error al abrir mapa: " + e.getMessage());
            Toast.makeText(this, "Error al abrir el mapa de variantes", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStreakAndData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Log.d("STREAK_FIX", "🎯 ACTUALIZANDO RACHA PARA: " + userId);

            // 1. ACTUALIZAR RACHA
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
                        streakDays.setText("1");
                        Toast.makeText(homeActivity.this, "Racha iniciada: 1 día", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            // 2. CARGAR DATOS EXISTENTES
            loadExistingUserData(userId);

        } else {
            Log.e("STREAK_FIX", "❌ Usuario no logueado");
            streakDays.setText("1");
        }
    }

    private void loadExistingUserData(String userId) {

        SharedPreferences prefs =
                getSharedPreferences("game_data", MODE_PRIVATE);

        long vidas = prefs.getLong("vidas", 5);

        runOnUiThread(() -> {
            diamondsCount.setText("0");
            livesCount.setText(String.valueOf(vidas));
        });
    }

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

        int[] titleIds = {R.id.level1Title, R.id.level2Title, R.id.level3Title,
                R.id.level4Title, R.id.level5Title, R.id.level6Title};

        int[] descIds = {R.id.level1Description, R.id.level2Description, R.id.level3Description,
                R.id.level4Description, R.id.level5Description, R.id.level6Description};

        for (int i = 0; i < levelIds.length; i++) {
            TextView title = findViewById(titleIds[i]);
            TextView desc = findViewById(descIds[i]);

            if (title != null) configuracionActivity.aplicarTamanioFuente(title, 16f);
            if (desc != null) configuracionActivity.aplicarTamanioFuente(desc, 14f);
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

        int[] titleIds = {R.id.level1Title, R.id.level2Title, R.id.level3Title,
                R.id.level4Title, R.id.level5Title, R.id.level6Title};

        int[] descIds = {R.id.level1Description, R.id.level2Description, R.id.level3Description,
                R.id.level4Description, R.id.level5Description, R.id.level6Description};

        int[] progressIds = {R.id.level1Progress, R.id.level2Progress, R.id.level3Progress,
                R.id.level4Progress, R.id.level5Progress, R.id.level6Progress};

        int[] statusIconIds = {R.id.level1StatusIcon, R.id.level2StatusIcon, R.id.level3StatusIcon,
                R.id.level4StatusIcon, R.id.level5StatusIcon, R.id.level6StatusIcon};

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

            TextView levelTitle = findViewById(titleIds[i]);
            TextView levelDesc = findViewById(descIds[i]);
            ProgressBar progressBar = findViewById(progressIds[i]);
            ImageView statusIcon = findViewById(statusIconIds[i]);

            if (levelTitle != null) {
                levelTitle.setText(String.format("Nivel %d", levelNumber));
            }

            if (levelDesc != null) {
                levelDesc.setText(descriptions[i]);
            }

            if (progressBar != null) {
                if (completado) {
                    progressBar.setProgress(100);
                    progressBar.setAlpha(1f);
                    progressBar.getProgressDrawable().setColorFilter(
                            ContextCompat.getColor(this, R.color.morado),
                            PorterDuff.Mode.SRC_IN);
                    totalCompletados++;
                } else {
                    progressBar.setProgress(0);
                    progressBar.setAlpha(0.5f);
                    progressBar.getProgressDrawable().setColorFilter(
                            ContextCompat.getColor(this, R.color.grey),
                            PorterDuff.Mode.SRC_IN);
                }
            }

            if (statusIcon != null) {
                if (completado) {
                    statusIcon.setImageResource(R.drawable.ic_check);
                } else {
                    statusIcon.setImageResource(R.drawable.ic_cross);
                }
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

                SharedPreferences prefs =
                        getSharedPreferences("game_data", MODE_PRIVATE);

                long vidas = prefs.getLong("vidas", 5);

                Intent intent = new Intent(this, ejercicio1.class);
                intent.putExtra("LEVEL_NUMBER", levelNumber);
                intent.putExtra("vidas", vidas);

                startActivity(intent);
            } else {
                Toast.makeText(this, "Nivel " + levelNumber + " en desarrollo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDraggableBubble(final CardView bubble, final boolean isReviewBubble) {
        bubble.setOnTouchListener(new View.OnTouchListener() {
            private boolean isDragging = false;
            private final int DRAG_THRESHOLD = 10;
            private float localDX = 0, localDY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        localDX = view.getX() - event.getRawX();
                        localDY = view.getY() - event.getRawY();
                        isDragging = false;
                        view.setElevation(20f);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() + localDX - view.getX());
                        float deltaY = Math.abs(event.getRawY() + localDY - view.getY());

                        if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                            isDragging = true;
                        }

                        float newX = event.getRawX() + localDX;
                        float newY = event.getRawY() + localDY;

                        ViewGroup parent = (ViewGroup) view.getParent();
                        int parentWidth = parent.getWidth();
                        int parentHeight = parent.getHeight();
                        int viewWidth = view.getWidth();
                        int viewHeight = view.getHeight();

                        if (newX < 0) {
                            newX = 0;
                        } else if (newX + viewWidth > parentWidth) {
                            newX = parentWidth - viewWidth;
                        }

                        if (newY < 0) {
                            newY = 0;
                        } else if (newY + viewHeight > parentHeight) {
                            newY = parentHeight - viewHeight;
                        }

                        view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();
                        break;

                    case MotionEvent.ACTION_UP:
                        view.setElevation(16f);

                        if (!isDragging) {
                            if (isReviewBubble) {
                                onReviewBubbleClicked();
                            } else {
                                onCuriositiesBubbleClicked();
                            }
                        }
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void onReviewBubbleClicked() {
        try {
            draggableBubble.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        draggableBubble.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();

                        openReviewActivity();
                    })
                    .start();
        } catch (Exception e) {
            Log.e("REVIEW_ERROR", "Error al abrir repaso: " + e.getMessage());
            Toast.makeText(this, "Error al abrir repaso", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCuriositiesBubbleClicked() {
        try {
            curiositiesBubble.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        curiositiesBubble.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();

                        openCuriositiesActivity();
                    })
                    .start();
        } catch (Exception e) {
            Log.e("CURIOSITIES_ERROR", "Error al abrir curiosidades: " + e.getMessage());
            Toast.makeText(this, "Error al abrir datos curiosos", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReviewActivity() {
        try {
            Intent intent = new Intent(this, ReviewActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Log.e("REVIEW_ERROR", "No se pudo abrir ReviewActivity: " + e.getMessage());
            Toast.makeText(this, "Función de repaso no disponible aún", Toast.LENGTH_SHORT).show();
            showTemporaryReview();
        }
    }

    private void openCuriositiesActivity() {
        try {
            Intent intent = new Intent(this, CuriositiesActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Log.e("CURIOSITIES_ERROR", "No se pudo abrir CuriositiesActivity: " + e.getMessage());
            Toast.makeText(this, "Función de datos curiosos no disponible aún", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTemporaryReview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Repaso")
                .setMessage("La función de repaso estará disponible pronto")
                .setPositiveButton("OK", null)
                .show();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_home) {
                        return true;
                    } else if (id == R.id.nav_dictionary) {
                        startActivity(new Intent(homeActivity.this, HerramientasActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_Minijuegos) {
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
        updateStreakAndData();
        setupLevelCards();
        aplicarFuentesAutomaticas();
    }
}