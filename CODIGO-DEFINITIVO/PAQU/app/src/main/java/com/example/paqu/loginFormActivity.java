package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginFormActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnIngresar, btnCrear;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Verificar si ya hay un usuario logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, homeActivity.class));
            finish();
            return;
        }

        // Inicializar vistas
        etEmail = findViewById(R.id.tvEmail);
        etPassword = findViewById(R.id.tvPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnCrear = findViewById(R.id.btnCrear);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();

            }
        });
        btnCrear.setOnClickListener(v -> {
            startActivity(new Intent(loginFormActivity.this, registroActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        // Validaciones
        if(email.isEmpty()) {
            etEmail.setError("Email es requerido");
            etEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un email válido");
            etEmail.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            etPassword.setError("Contraseña es requerida");
            etPassword.requestFocus();
            return;
        }

        if(password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("INGRESAR");

                    if (task.isSuccessful()) {
                        Intent i = new Intent(loginFormActivity.this, homeActivity.class);
                        startActivity(i);

                    } else {
                        Toast.makeText(loginFormActivity.this,
                                "Credenciales incorrectas", // Mensaje más amigable
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya está logueado al iniciar la actividad
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, homeActivity.class));
            finish();
        }
    }
}