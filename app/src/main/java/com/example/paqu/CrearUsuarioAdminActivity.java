package com.example.paqu;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioAdminActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etPassword;
    private Spinner spinnerRol;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario_admin);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initViews();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerRol = findViewById(R.id.spinnerRol);

        // Configurar spinner de roles
        String[] roles = {"usuario_comun", "docente", "administrador"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        MaterialButton btnCrear = findViewById(R.id.btnCrear);
        btnCrear.setOnClickListener(v -> validarYCrear());
    }

    private void validarYCrear() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rol = spinnerRol.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingrese nombre");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }

        crearUsuario(email, password, nombre, rol);
    }

    private void crearUsuario(String email, String password, String nombre, String rol) {
        // Crear en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // Crear en Realtime Database
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", uid);
                    userData.put("email", email);
                    userData.put("name", nombre);
                    userData.put("role", rol);
                    userData.put("createdAt", System.currentTimeMillis());
                    userData.put("createdBy", mAuth.getCurrentUser().getUid());

                    // Estructura inicial
                    Map<String, Object> progress = new HashMap<>();
                    progress.put("hearts", 5);
                    progress.put("coins", 0);
                    progress.put("level", 1);
                    progress.put("totalXP", 0);
                    userData.put("progress", progress);

                    Map<String, Object> streak = new HashMap<>();
                    streak.put("currentStreak", 0);
                    streak.put("longestStreak", 0);
                    streak.put("lastActiveDate", System.currentTimeMillis());
                    userData.put("streak", streak);

                    usersRef.child(uid).setValue(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Usuario creado: " + nombre, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error BD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error Auth: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}