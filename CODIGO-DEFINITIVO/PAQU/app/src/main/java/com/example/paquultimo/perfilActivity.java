package com.example.paquultimo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class perfilActivity extends BaseActivity {

    ImageButton tuercaIcon;
    Button btnAgregarAmigos, btnCompartirPerfil, btnEscojeAvatar;
    TextView tvUsuario, tvDescripcion, tvSiguiendo, tvSeguidores, tvExp, tvPuntaje;
    ImageView imageViewAvatar;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private static final int REQUEST_CODE_SELECT_AVATAR = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        tuercaIcon = findViewById(R.id.tuercaIcon);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvSiguiendo = findViewById(R.id.tvSiguiendo);
        tvSeguidores = findViewById(R.id.tvSeguidores);
        tvExp = findViewById(R.id.tvExp);
        tvPuntaje = findViewById(R.id.tvPuntaje);
        imageViewAvatar = findViewById(R.id.imageView3);
        btnEscojeAvatar = findViewById(R.id.EscojeAvatar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar avatar guardado al iniciar
        cargarAvatarGuardado();

        // Configurar click listener para el botón de elegir avatar
        btnEscojeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(perfilActivity.this, avatarActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_AVATAR);
            }
        });

        FirebaseUser user = firebaseAuth.getCurrentUser(); // ✅ Obtener el usuario

        if (user != null) {
            String nombre = user.getDisplayName();

            if (nombre != null && !nombre.isEmpty()) {
                tvUsuario.setText(nombre);
                tvDescripcion.setText(nombre + " se unió en " + getFechaFormateada(user));
            } else {
                String uid = user.getUid();
                databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String nombreDB = snapshot.child("nombre").getValue(String.class);
                            if (nombreDB != null) {
                                tvUsuario.setText(nombreDB);
                                tvDescripcion.setText(nombreDB + " se unió en " + getFechaFormateada(user));
                            } else {
                                tvUsuario.setText("Usuario");
                                tvDescripcion.setText("Usuario se unió en " + getFechaFormateada(user));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(perfilActivity.this, "Error al cargar nombre", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Obtener SharedPreferences
        SharedPreferences prefs = getSharedPreferences("progreso", MODE_PRIVATE);

        // Leer valores guardados
        int expTotal = prefs.getInt("expTotal", 0);
        int rachaActual = prefs.getInt("rachaActual", 0);

        // Mostrar en los TextView correspondientes
        tvExp.setText(String.valueOf(expTotal));
        tvPuntaje.setText(String.valueOf(rachaActual));

        tuercaIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(perfilActivity.this, "¡Configuración!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(perfilActivity.this, configuracionActivity.class);
                startActivity(intent);
            }
        });

        btnAgregarAmigos = findViewById(R.id.btnAgregarAmigos);
        btnCompartirPerfil = findViewById(R.id.btnCompartirPerfil);

        btnCompartirPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(perfilActivity.this, "Compartiendooo...",Toast.LENGTH_SHORT).show();
            }
        });

        btnAgregarAmigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(perfilActivity.this, "Agregand amigoss...",Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para cargar el avatar guardado
    private void cargarAvatarGuardado() {
        SharedPreferences prefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        int avatarResId = prefs.getInt("selected_avatar", R.drawable.llamaparaperifl);
        imageViewAvatar.setImageResource(avatarResId);
    }

    // Recibir el resultado de avatarActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_AVATAR && resultCode == RESULT_OK) {
            if (data != null) {
                int selectedAvatar = data.getIntExtra("selected_avatar", R.drawable.llamaparaperifl);
                imageViewAvatar.setImageResource(selectedAvatar);

                // Opcional: Guardar también en Firebase
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
                        // Toast opcional: Toast.makeText(this, "Avatar actualizado en Firebase", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar avatar en Firebase", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFechaFormateada(FirebaseUser user) {
        long creationTimestamp = user.getMetadata().getCreationTimestamp();
        Date creationDate = new Date(creationTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return sdf.format(creationDate);
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_profile;
    }
}