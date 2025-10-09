    package com.example.paqu;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.graphics.Color;
    import android.graphics.PorterDuff;
    import android.os.Bundle;
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

            // Configurar barra de navegación inferior
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);



            // Obtener colores
            stickyColor = ContextCompat.getColor(this, R.color.rosado); // Color morado para sticky

            // Configurar el efecto sticky con cambio de color
            setupStickyHeader();

            // Actualizar datos del usuario
            updateUserData();

            // Configurar niveles clickeables
            setupLevelCards();
        }

        private void setupStickyHeader() {
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

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(homeActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
                            ContextCompat.getColor(this, R.color.grey), // define color gris en `colors.xml`
                            PorterDuff.Mode.SRC_IN);
                    statusIcon.setImageResource(R.drawable.ic_cross);
                }

                levelCard.setOnClickListener(v -> {
                    navigateToExercise(levelNumber);
                });
            }




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
                        Toast.makeText(this, "Nivel 1 en desarrollo", Toast.LENGTH_SHORT).show();
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(homeActivity.this, "Error al cargar progreso", Toast.LENGTH_SHORT).show();
                    }
                });
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
        }


    }