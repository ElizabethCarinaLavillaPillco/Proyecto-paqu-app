package com.example.paqu;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AdminCrearUsuarioActivity extends AppCompatActivity {

    private static final String TAG = "AdminCrearUsuario";

    private EditText etNombre, etEmail, etEdad, etPassword;
    private Spinner spinnerRol;
    private Button btnCrear;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_crear_usuario);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombreNuevo);
        etEmail = findViewById(R.id.etEmailNuevo);
        etEdad = findViewById(R.id.etEdadNuevo);
        etPassword = findViewById(R.id.etPasswordNuevo);
        spinnerRol = findViewById(R.id.spinnerRolNuevo);
        btnCrear = findViewById(R.id.btnCrearUsuario);

        ImageView btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        String[] roles = {"Estudiante", "Docente", "Administrador"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);
    }

    private void setupListeners() {
        btnCrear.setOnClickListener(v -> crearUsuario());
    }

    private void crearUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String edad = etEdad.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rolSeleccionado = spinnerRol.getSelectedItem().toString();

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa el nombre");
            etNombre.requestFocus();
            return;
        }

        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            etEmail.setError("Ingresa un email válido");
            etEmail.requestFocus();
            return;
        }

        if (edad.isEmpty()) {
            etEdad.setError("Ingresa la edad");
            etEdad.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener mínimo 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        // Convertir rol seleccionado a valor de BD
        String rolBD;
        switch (rolSeleccionado) {
            case "Docente": rolBD = "docente"; break;
            case "Administrador": rolBD = "administrador"; break;
            default: rolBD = "usuario_comun";
        }

        // Mostrar progreso
        btnCrear.setEnabled(false);
        btnCrear.setText("Creando...");

        // Crear usuario en Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Usuario creado en Auth: " + user.getUid());
                            // Guardar datos en Realtime Database
                            guardarUsuarioEnBD(user.getUid(), nombre, email, edad, rolBD);
                        } else {
                            mostrarError("Error al obtener datos del usuario");
                        }
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error creando usuario: " + errorMsg);
                        mostrarError(errorMsg);
                    }
                });
    }

    private void guardarUsuarioEnBD(String uid, String nombre, String email, String edad, String rol) {
        Log.d(TAG, "📝 Guardando usuario en BD: " + uid);

        // 🔹 Crear estructura completa del usuario
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("name", nombre);
        usuarioData.put("email", email);
        usuarioData.put("role", rol);
        usuarioData.put("createdAt", System.currentTimeMillis());

        // Datos adicionales
        Map<String, Object> progress = new HashMap<>();
        progress.put("hearts", 5);
        progress.put("coins", 100);
        progress.put("level", 1);
        progress.put("totalXP", 0);
        progress.put("experience", 0);

        Map<String, Object> streak = new HashMap<>();
        streak.put("currentStreak", 0);
        streak.put("longestStreak", 0);
        streak.put("lastActiveDate", "");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", uid);
        userInfo.put("email", email);
        userInfo.put("avatar", "");
        userInfo.put("language", "es");
        userInfo.put("notificationsEnabled", true);

        // Estructura completa
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", nombre);
        userData.put("email", email);
        userData.put("role", rol);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("progress", progress);
        userData.put("streak", streak);
        userData.put("userInfo", userInfo);

        // 🔹 Guardar en el nodo "users" (minúscula)
        databaseRef.child("users").child(uid).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Usuario guardado en BD correctamente");

                    // Actualizar perfil en Firebase Auth
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombre)
                                        .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        Log.d(TAG, "✅ Perfil actualizado");
                                    } else {
                                        Log.e(TAG, "❌ Error actualizando perfil: " +
                                                profileTask.getException().getMessage());
                                    }
                                });
                    }

                    Toast.makeText(this, "✅ Usuario creado exitosamente como " + rol,
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al guardar en BD: " + e.getMessage());
                    mostrarError("Error al guardar datos: " + e.getMessage());
                });
    }

    private void mostrarError(String mensaje) {
        btnCrear.setEnabled(true);
        btnCrear.setText("Crear Usuario");
        Toast.makeText(this, "❌ " + mensaje, Toast.LENGTH_LONG).show();
    }
}