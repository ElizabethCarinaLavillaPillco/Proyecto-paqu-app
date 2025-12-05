package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paqu.managers.FavoritosManager;
import com.example.paqu.managers.FirebaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class perfilActivity extends BaseActivity {

    ImageButton tuercaIcon;
    MaterialButton btnAgregarAmigos, btnCompartirPerfil, btnEscojeAvatar;
    TextView tvUsuario, tvDescripcion, tvSiguiendo, tvSeguidores, tvExp, tvPuntaje;
    TextView tvCantidadFavoritos;
    CardView cardFavoritos;
    ImageView imageViewAvatar;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FavoritosManager favoritosManager;
    private static final int REQUEST_CODE_SELECT_AVATAR = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        initViews();
        initManagers();
        aplicarFuentesAutomaticas();
        setupListeners();
        loadUserData();
        loadFavoritosCount();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tuercaIcon = findViewById(R.id.tuercaIcon);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvSiguiendo = findViewById(R.id.tvSiguiendo);
        tvSeguidores = findViewById(R.id.tvSeguidores);
        tvExp = findViewById(R.id.tvExp);
        tvPuntaje = findViewById(R.id.tvPuntaje);
        tvCantidadFavoritos = findViewById(R.id.tvCantidadFavoritos);
        cardFavoritos = findViewById(R.id.cardFavoritos);
        imageViewAvatar = findViewById(R.id.imageView3);
        btnEscojeAvatar = findViewById(R.id.EscojeAvatar);
        btnAgregarAmigos = findViewById(R.id.btnAgregarAmigos);
        btnCompartirPerfil = findViewById(R.id.btnCompartirPerfil);
    }

    private void initManagers() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");
        favoritosManager = new FavoritosManager();
    }

    private void setupListeners() {
        tuercaIcon.setOnClickListener(v -> {
            Toast.makeText(this, "¡Configuración!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, configuracionActivity.class));
        });

        btnEscojeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, avatarActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT_AVATAR);
        });

        btnAgregarAmigos.setOnClickListener(v -> {
            Toast.makeText(this, "Agregando amigos...", Toast.LENGTH_SHORT).show();
        });

        btnCompartirPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "Compartiendo...", Toast.LENGTH_SHORT).show();
        });

        // ✅ Click en card de favoritos
        cardFavoritos.setOnClickListener(v -> {
            animarClick(v);
            Intent intent = new Intent(this, FavoritosActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        cargarAvatarGuardado();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            Log.d("FIREBASE_DEBUG", "✅ Usuario autenticado: " + userId);

            // Cargar nombre de usuario
            String nombre = user.getDisplayName();
            if (nombre != null && !nombre.isEmpty()) {
                tvUsuario.setText(nombre);
                tvDescripcion.setText(nombre + " • Se unió en " + getFechaFormateada(user));
            } else {
                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String nombreDB = snapshot.child("nombre").getValue(String.class);
                            if (nombreDB != null) {
                                tvUsuario.setText(nombreDB);
                                tvDescripcion.setText(nombreDB + " • Se unió en " + getFechaFormateada(user));
                            } else {
                                String email = user.getEmail();
                                String nombreUsuario = (email != null) ? email.split("@")[0] : "Usuario";
                                tvUsuario.setText(nombreUsuario);
                                tvDescripcion.setText(nombreUsuario + " • Se unió en " + getFechaFormateada(user));
                                crearUsuarioEnFirebase(userId, user, nombreUsuario);
                            }
                        } else {
                            String email = user.getEmail();
                            String nombreUsuario = (email != null) ? email.split("@")[0] : "Usuario";
                            tvUsuario.setText(nombreUsuario);
                            tvDescripcion.setText(nombreUsuario + " • Se unió en " + getFechaFormateada(user));
                            crearUsuarioEnFirebase(userId, user, nombreUsuario);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FIREBASE_DEBUG", "❌ Error BD: " + error.getMessage());
                    }
                });
            }

            // ✅ CARGAR ESTADÍSTICAS DESDE FIREBASE
            loadFirebaseStats(userId);

        } else {
            tvUsuario.setText("Invitado");
            tvDescripcion.setText("Inicia sesión para personalizar tu perfil");
            btnEscojeAvatar.setEnabled(false);
            tvExp.setText("0");
            tvPuntaje.setText("0");
        }
    }

    // ✅ NUEVO MÉTODO: Cargar estadísticas desde Firebase
    private void loadFirebaseStats(String userId) {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.getUserProgress(userId, new FirebaseManager.ProgressCallback() {
            @Override
            public void onSuccess(FirebaseManager.UserProgress progress) {
                runOnUiThread(() -> {
                    // Actualizar EXP
                    int experience = progress.experience != null ? progress.experience : 0;
                    tvExp.setText(String.valueOf(experience));
                    Log.d("FIREBASE_DEBUG", "✅ EXP cargado: " + experience);

                    // Actualizar Racha
                    int streak = progress.currentStreak != null ? progress.currentStreak : 0;
                    tvPuntaje.setText(String.valueOf(streak));
                    Log.d("FIREBASE_DEBUG", "✅ Racha cargada: " + streak);

                    // Guardar en SharedPreferences como backup
                    SharedPreferences prefs = getSharedPreferences("progreso", MODE_PRIVATE);
                    prefs.edit()
                            .putInt("expTotal", experience)
                            .putInt("rachaActual", streak)
                            .apply();
                });
            }

            @Override
            public void onError(String error) {
                Log.e("FIREBASE_DEBUG", "❌ Error al cargar estadísticas: " + error);

                // Fallback a SharedPreferences
                runOnUiThread(() -> {
                    SharedPreferences prefs = getSharedPreferences("progreso", MODE_PRIVATE);
                    int expTotal = prefs.getInt("expTotal", 0);
                    int rachaActual = prefs.getInt("rachaActual", 0);

                    tvExp.setText(String.valueOf(expTotal));
                    tvPuntaje.setText(String.valueOf(rachaActual));
                });
            }
        });
    }

    // ✅ Cargar cantidad de favoritos
    private void loadFavoritosCount() {
        favoritosManager.obtenerCantidadFavoritos(cantidad -> {
            runOnUiThread(() -> {
                String texto = cantidad + (cantidad == 1 ? " palabra guardada" : " palabras guardadas");
                tvCantidadFavoritos.setText(texto);
            });
        });
    }

    private void aplicarFuentesAutomaticas() {
        aplicarFuente(R.id.tvUsuario, 22f);
        aplicarFuente(R.id.tvDescripcion, 14f);
        aplicarFuente(R.id.tvSiguiendo, 28f);
        aplicarFuente(R.id.tvSeguidores, 28f);
        aplicarFuente(R.id.tvExp, 20f);
        aplicarFuente(R.id.tvPuntaje, 20f);
        aplicarFuente(R.id.tvCantidadFavoritos, 12f);
        aplicarFuenteBotones();
    }

    private void aplicarFuenteBotones() {
        int[] botonesIds = {
                R.id.EscojeAvatar,
                R.id.btnAgregarAmigos
        };

        for (int botonId : botonesIds) {
            MaterialButton boton = findViewById(botonId);
            if (boton != null) {
                configuracionActivity.aplicarTamanioFuente(boton, 14f);
            }
        }
    }

    private void aplicarFuente(int textViewId, float tamanioBase) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            configuracionActivity.aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarFuentesAutomaticas();
        loadFavoritosCount();

        // ✅ Recargar estadísticas al volver
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            loadFirebaseStats(user.getUid());
        }
    }

    private void crearUsuarioEnFirebase(String uid, FirebaseUser user, String nombreUsuario) {
        DatabaseReference userRef = databaseReference.child(uid);

        User usuario = new User();
        usuario.setNombre(nombreUsuario);
        usuario.setEmail(user.getEmail());
        usuario.setSeguidores(0);
        usuario.setSiguiendo(0);
        usuario.setAvatar(R.drawable.llamaparaperifl);

        userRef.setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_DEBUG", "✅ Usuario creado en BD: " + nombreUsuario);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_DEBUG", "❌ Error al crear usuario: " + e.getMessage());
                });
    }

    private void cargarAvatarGuardado() {
        SharedPreferences prefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        int avatarResId = prefs.getInt("selected_avatar", R.drawable.llamaparaperifl);
        imageViewAvatar.setImageResource(avatarResId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_AVATAR && resultCode == RESULT_OK) {
            if (data != null) {
                int selectedAvatar = data.getIntExtra("selected_avatar", R.drawable.llamaparaperifl);
                imageViewAvatar.setImageResource(selectedAvatar);
                guardarAvatarEnFirebase(selectedAvatar);
            }
        }
    }

    private void guardarAvatarEnFirebase(int avatarResId) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            databaseReference.child(userId).child("avatar").setValue(avatarResId)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FIREBASE_DEBUG", "✅ Avatar guardado en Firebase");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE_DEBUG", "❌ Error al guardar avatar");
                    });
        }
    }

    private String getFechaFormateada(FirebaseUser user) {
        long creationTimestamp = user.getMetadata().getCreationTimestamp();
        Date creationDate = new Date(creationTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        return sdf.format(creationDate);
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_profile;
    }
}