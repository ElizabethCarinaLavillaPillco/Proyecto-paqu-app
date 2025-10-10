<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
    package com.example.paquultimo;
=======
package com.example.paqu;
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class homeActivity extends BaseActivity {

    private CardView stickySection;
    private LinearLayout sectionContent;
    private int stickySectionTop;
    private int originalColor;
    private int stickyColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
            // Configurar barra de navegación inferior
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);

            // Referencias a las vistas sticky
            stickySection = findViewById(R.id.stickySection);
            sectionContent = findViewById(R.id.sectionContent);

            // Obtener colores
            stickyColor = ContextCompat.getColor(this, R.color.rosado); // Color morado para sticky

            // Configurar el efecto sticky con cambio de color
            setupStickyHeader();

            // Actualizar datos del usuario
            updateUserData();

            // Configurar niveles clickeables
            setupLevelCards();
=======
        // ✅ VERIFICACIÓN DE USUARIO - AGREGAR ESTO
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("HOME_DEBUG", "✅ Usuario autenticado en home: " + user.getEmail());
            Log.d("HOME_DEBUG", "🆔 UID: " + user.getUid());
        } else {
            Log.d("HOME_DEBUG", "❌ No hay usuario en home - Redirigiendo al login");
            // Si por algún error llega aquí sin usuario, redirigir al login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java
        }

        // Configurar barra de navegación inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Inicializar vistas para el sticky header
        stickySection = findViewById(R.id.stickySection);
        sectionContent = findViewById(R.id.sectionContent);

        // Obtener colores
        originalColor = Color.TRANSPARENT; // O el color que tengas definido
        stickyColor = ContextCompat.getColor(this, R.color.rosado); // Color morado para sticky

        // Configurar el efecto sticky con cambio de color
        setupStickyHeader();

        // Actualizar datos del usuario
        updateUserData();

        // Configurar niveles clickeables
        setupLevelCards();
    }

    private void setupStickyHeader() {
        if (stickySection == null || sectionContent == null) {
            Log.e("HOME_DEBUG", "❌ stickySection o sectionContent es null");
            return;
        }

        final ViewTreeObserver observer = stickySection.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                stickySectionTop = stickySection.getTop();
                stickySection.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Configurar scroll listener
                findViewById(R.id.mainScrollView).getViewTreeObserver()
                        .addOnScrollChangedListener(() -> {
                            int scrollY = findViewById(R.id.mainScrollView).getScrollY();

                            // Cambiar color y posición cuando se hace scroll
                            if (scrollY >= stickySectionTop) {
                                // Cambiar color a morado
                                sectionContent.setBackgroundColor(stickyColor);
                            } else {
                                // Volver a posición normal
                                stickySection.setTranslationY(0);
                                // Restaurar color original
                                sectionContent.setBackgroundColor(originalColor);
                            }
                        });
            }
        });
    }

    private void updateUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Usuarios")
                    .child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Long racha = snapshot.child("racha").getValue(Long.class);
                        Long diamantes = snapshot.child("diamantes").getValue(Long.class);
                        Long vidas = snapshot.child("vidas").getValue(Long.class);

                        Log.d("HOME_DEBUG", "✅ Datos de usuario cargados correctamente");
                        // Aquí puedes actualizar tus TextView con los datos
                    } else {
                        Log.d("HOME_DEBUG", "ℹ️ Usuario no existe en BD, creando...");
                        crearUsuarioEnFirebase(uid, user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HOME_DEBUG", "❌ Error al obtener datos del usuario: " + error.getMessage());
                    Toast.makeText(homeActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
        private void updateUserData() {
            TextView streakDays = findViewById(R.id.streakDays);
            TextView diamondsCount = findViewById(R.id.diamondsCount);
            TextView livesCount = findViewById(R.id.livesCount);
=======
    private void crearUsuarioEnFirebase(String uid, FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(uid);
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java

        // Crear objeto usuario con datos básicos
        User usuario = new User();
        usuario.setNombre(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail().split("@")[0]);
        usuario.setEmail(user.getEmail());
        usuario.setSeguidores(0);
        usuario.setSiguiendo(0);
        usuario.setAvatar(R.drawable.llamaparaperifl);

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
            if (user != null) {
                String uid = user.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("Usuarios")
                        .child(uid);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long racha = snapshot.child("racha").getValue(Long.class);
                            Long diamantes = snapshot.child("diamantes").getValue(Long.class);
                            Long vidas = snapshot.child("vidas").getValue(Long.class);

                            streakDays.setText(racha != null ? String.valueOf(racha) : "0");
                            diamondsCount.setText(diamantes != null ? String.valueOf(diamantes) : "0");
                            livesCount.setText(vidas != null ? String.valueOf(vidas) : "0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(homeActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                    }
=======
        userRef.setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d("HOME_DEBUG", "✅ Usuario creado en BD: " + usuario.getNombre());
                })
                .addOnFailureListener(e -> {
                    Log.e("HOME_DEBUG", "❌ Error al crear usuario: " + e.getMessage());
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java
                });
    }

    private void setupLevelCards() {
        int[] levelCardIds = {R.id.level1Card};

        String[] descriptions = {
                "Saludos básicos",
                "Presentaciones",
                "Familia",
                "Números",
                "Colores",
                "Comida"
        };

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
        private void setupLevelCards() {
            int[] levelCardIds = {R.id.level1Card, R.id.level2Card, R.id.level3Card,
                    R.id.level4Card, R.id.level5Card, R.id.level6Card};
=======
        SharedPreferences prefs = getSharedPreferences("niveles", MODE_PRIVATE);
        int totalCompletados = 0;
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java

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

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
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
                            ContextCompat.getColor(this, R.color.grey), // define color gris en `colors.xml`
                            PorterDuff.Mode.SRC_IN);
                    statusIcon.setImageResource(R.drawable.ic_cross);
                }

                levelCard.setOnClickListener(v -> {
                    navigateToExercise(levelNumber);
                });
            }

            // Actualizar barra de la sección
            ProgressBar sectionProgressBar = findViewById(R.id.sectionProgressBar);
            if (sectionProgressBar != null) {
                float progreso = (float) totalCompletados / 6f;
                int porcentaje = Math.round(progreso * 100); // 1/6 ≈ 17%

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

        private void updateProgressAppearance(ProgressBar progressBar, int progress) {
            if (progress <= 0) {
                // Nivel bloqueado
                progressBar.setProgress(0);
                progressBar.setAlpha(0.5f); // Hacer más transparente
            } else if (progress >= 100) {
                // Nivel completado
=======
            if (completado) {
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java
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

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
                switch(levelNumber) {
                    case 1:
                        intent = new Intent(this, ejercicio1.class);
                        intent.putExtra("LEVEL_NUMBER", levelNumber);
                        startActivity(intent);
                        break;

                    case 2:
                        // Para cuando implementes el nivel 2
                        // intent = new Intent(this, ejercicio2.class);
                        Toast.makeText(this, "Nivel 2 en desarrollo", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(this, "Nivel no disponible aún", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
=======
            levelCard.setOnClickListener(v -> {
                navigateToExercise(levelNumber);
            });
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java
        }

        Log.d("HOME_DEBUG", "🎯 Niveles configurados: " + totalCompletados + " completados");
    }

    private void updateProgressAppearance(ProgressBar progressBar, int progress) {
        if (progress <= 0) {
            // Nivel bloqueado
            progressBar.setProgress(0);
            progressBar.setAlpha(0.5f); // Hacer más transparente
        } else if (progress >= 100) {
            // Nivel completado
            progressBar.setProgress(100);
            progressBar.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.verde),
                    PorterDuff.Mode.SRC_IN);
        } else {
            // Nivel en progreso
            progressBar.setProgress(progress);
            progressBar.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.morado),
                    PorterDuff.Mode.SRC_IN);
        }
    }

    private void navigateToExercise(int levelNumber) {
        try {
            Intent intent;

            switch(levelNumber) {
                case 1:
                    // ✅ CORREGIDO: Abrir ejercicio1 en lugar del mensaje
                    intent = new Intent(this, ejercicio1.class);
                    startActivity(intent);
                    break;

                case 2:
                    // ✅ CORREGIDO: Abrir ejercicio2
                    intent = new Intent(this, ejercicio2.class);
                    startActivity(intent);
                    break;

                default:
                    Toast.makeText(this, "Nivel no disponible aún", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir el nivel: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void loadProgressFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("progress");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot levelSnapshot : snapshot.getChildren()) {
                        int level = Integer.parseInt(levelSnapshot.getKey().replace("level", ""));
                        int progress = levelSnapshot.getValue(Integer.class);

                        // Actualizar la barra de progreso correspondiente
                        if (level >= 1 && level <= 6) {
                            CardView levelCard = findViewById(getResources()
                                    .getIdentifier("level" + level + "Card", "id", getPackageName()));
                            if (levelCard != null) {
                                ProgressBar pb = levelCard.findViewById(R.id.levelProgress);
                                pb.setProgress(progress);
                                updateProgressAppearance(pb, progress);
                            }
                        }
                    }
                }

<<<<<<< HEAD:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paquultimo/homeActivity.java
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(homeActivity.this, "Error al cargar progreso", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private BottomNavigationView.OnNavigationItemSelectedListener navListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.nav_home) {
                            // Ya estamos en home
                            return true;
                        } else if (id == R.id.nav_dictionary) {
                            Intent intent = new Intent(homeActivity.this, diccionarioActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        } else if (id == R.id.nav_minijuegos) {
                            // CORREGIDO: Ahora apunta a MiniJuegosActivity
                            Intent intent = new Intent(homeActivity.this, MiniJuegosActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        } else if (id == R.id.nav_profile) {
                            Intent intent = new Intent(homeActivity.this, perfilActivity.class);
                            startActivity(intent);
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
            updateUserData();     // ← Asegúrate de que esta línea esté aquí
            setupLevelCards(); // Recargar progreso
=======
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(homeActivity.this, "Error al cargar progreso", Toast.LENGTH_SHORT).show();
                }
            });
>>>>>>> cceefd0682b9a7ebb02102a91f71f5bbe0cffd38:CODIGO-DEFINITIVO/PAQU/app/src/main/java/com/example/paqu/homeActivity.java
        }
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserData();     // ← Asegúrate de que esta línea esté aquí
        setupLevelCards(); // Recargar progreso
        Log.d("HOME_DEBUG", "🔄 homeActivity onResume ejecutado");
    }
}