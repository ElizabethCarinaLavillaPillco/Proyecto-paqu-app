package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class loginFormActivity extends AppCompatActivity {

    private EditText tvEmail, tvPassword;
    private Button btnIngresar, btnCrear;
    private TextView tvRecuperarPassword;
    private SignInButton btnGoogle;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Referencias UI (NO SE CAMBIA NINGÚN ID)
        tvEmail = findViewById(R.id.tvEmail);
        tvPassword = findViewById(R.id.tvPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnCrear = findViewById(R.id.btnCrear);
        tvRecuperarPassword = findViewById(R.id.tvRecuperarPassword);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Configurar Google Sign-In
        configurarLoginGoogle();

        // Botón normal: iniciar sesión con correo y contraseña
        btnIngresar.setOnClickListener(v -> loginConCorreo());

        // Botón: ir a registro
        btnCrear.setOnClickListener(v -> {
            Intent intent = new Intent(loginFormActivity.this, registroActivity.class);
            startActivity(intent);
            finish();
        });

        // Recuperar contraseña (opcional)
        tvRecuperarPassword.setOnClickListener(v ->
                Toast.makeText(this, "Función en desarrollo: recuperar contraseña", Toast.LENGTH_SHORT).show()
        );

        // Si ya está logueado, ir directo al home
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            redirigirAHome();
        }
    }

    private void configurarLoginGoogle() {
        try {
            Log.d("GOOGLE_LOGIN", "Configurando Google Sign-In...");

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);

            btnGoogle.setOnClickListener(v -> {
                Log.d("GOOGLE_LOGIN", "Botón Google presionado");
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });

        } catch (Exception e) {
            Log.e("GOOGLE_LOGIN", "Error al configurar Google: " + e.getMessage());
        }
    }

    private void loginConCorreo() {
        String correo = tvEmail.getText().toString().trim();
        String contra = tvPassword.getText().toString().trim();

        if (correo.isEmpty() || contra.isEmpty()) {
            Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(correo, contra)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            redirigirAHome();
                        }
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d("GOOGLE_LOGIN", "Resultado del intento de Google recibido");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e("GOOGLE_LOGIN", "Error al iniciar sesión con Google: " + e.getMessage());
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("GOOGLE_LOGIN", "Autenticando con Firebase...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = firebaseAuth.getCurrentUser();
                        if (usuario != null) {
                            redirigirAHome();
                        }
                    } else {
                        Toast.makeText(this, "Autenticación con Google fallida", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 Redirección directa al Home (sin pantallas intermedias)
    private void redirigirAHome() {
        Intent intent = new Intent(loginFormActivity.this, homeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
