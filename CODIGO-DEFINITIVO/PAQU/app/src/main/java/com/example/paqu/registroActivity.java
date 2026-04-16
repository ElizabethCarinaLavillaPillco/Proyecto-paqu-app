package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registroActivity extends AppCompatActivity {
    // Componentes de UI
    private EditText txtNombre, txtEdad, txtCorreo, txtContra;
    private Button btnIngresarApp, btnCerrar;
    private SignInButton btnGoogle;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    // Login con Google
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;

    // Login con Facebook
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        // Verificación temporal
        verificarConfiguracionGoogle();

        // Inicialización de vistas
        btnIngresarApp = findViewById(R.id.btnIngresarApp);
        btnCerrar = findViewById(R.id.btnCerrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtNombre = findViewById(R.id.txtNombre);
        txtEdad = findViewById(R.id.txtEdad);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtContra = findViewById(R.id.txtContra);

        // Inicialización de Firebase
        inicializarFirebase();
        firebaseAuth = FirebaseAuth.getInstance();

        // Configuración de listeners
        configurarLoginGoogle();
        configurarLoginFacebook();


        btnIngresarApp.setOnClickListener(v -> registrarUsuario());

        btnCerrar.setOnClickListener(v -> {
            startActivity(new Intent(registroActivity.this, profileActivity.class));
            finish();
        });
    }

    private void verificarConfiguracionGoogle() {
        try {
            String clientId = getString(R.string.default_web_client_id);
            Log.d("GOOGLE_CONFIG", "✅ Client ID: " + clientId);
            Log.d("GOOGLE_CONFIG", "✅ SHA-1 en google-services.json: a9d51cb7dd9867ca27487504712a25710f2bbcb2");

            if (clientId.equals("507103280595-9d5ca90anqs9vs4qhf63roaulbhg0d6f.apps.googleusercontent.com")) {
                Log.d("GOOGLE_CONFIG", "✅ TODO CONFIGURADO CORRECTAMENTE");
            }
        } catch (Exception e) {
            Log.e("GOOGLE_CONFIG", "❌ Error: " + e.getMessage());
        }
    }
    private void registrarUsuario() {
        String nombre = txtNombre.getText().toString().trim();
        String edad = txtEdad.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String contra = txtContra.getText().toString().trim();

        if (!validarFormulario(nombre, edad, correo, contra)) {
            return;
        }

        // 1. Registrar usuario en Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(correo, contra)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        // 2. Guardar datos adicionales en Realtime Database
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if (user != null) {

                            // 1. Guardar en la base de datos

                            guardarDatosUsuario(user.getUid(), nombre, edad, correo);
                            redirigirABienvenida(nombre);

                            Toast.makeText(this, "Usuario creado. Procediendo a guardar datos...", Toast.LENGTH_SHORT).show();



                            // 2. Guardar también en Firebase Authentication
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            redirigirABienvenida(nombre);
                                        } else {
                                            Exception e = task.getException();
                                            Toast.makeText(this, "Error al guardar nombre en Authentication", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }


                    } else {
                        mostrarErrorRegistro(task.getException());
                    }
                });
    }

    private boolean validarFormulario(String nombre, String edad, String correo, String contra) {
        if (nombre.isEmpty() || edad.isEmpty() || correo.isEmpty() || contra.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void guardarDatosUsuario(String uid, String nombre, String edad, String correo) {
        Usuario usuario = new Usuario(nombre, correo, edad);

        databaseReference.child("Usuarios").child(uid)
                .setValue(usuario)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        redirigirABienvenida(nombre);
                        Toast.makeText(this, "Guardando en Realtime DB...", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void redirigirABienvenida(String nombre) {
        Intent intent = new Intent(this, logeocompletoActivity.class);
        intent.putExtra("nombre", nombre);
        startActivity(intent);
    }

    private void mostrarErrorRegistro(Exception exception) {
        String errorMessage = "Error en registro";
        if (exception != null) {
            errorMessage = exception.getMessage();
            if (errorMessage.contains("email address is badly formatted")) {
                errorMessage = "Formato de correo inválido";
            } else if (errorMessage.contains("password is weak")) {
                errorMessage = "La contraseña debe tener al menos 6 caracteres";
            } else if (errorMessage.contains("email address is already in use")) {
                errorMessage = "El correo ya está registrado";
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance("https://paqu-df872-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();
    }

    private void configurarLoginGoogle() {
        try {
            Log.d("GOOGLE_DEBUG", "🔧 Configurando Google Sign-In...");

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d("GOOGLE_DEBUG", "✅ Google Sign-In configurado");

            btnGoogle.setOnClickListener(v -> {
                Log.d("GOOGLE_DEBUG", "🎯 Botón Google presionado");
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });

        } catch (Exception e) {
            Log.e("GOOGLE_DEBUG", "❌ Error: " + e.getMessage());
        }
    }

    private void configurarLoginFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Implementar autenticación con Firebase si es necesario
                        Toast.makeText(registroActivity.this, "Login con Facebook exitoso", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(registroActivity.this, "Login cancelado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(registroActivity.this, "Error en Facebook Login", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Para login con Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Error en login con Google", Toast.LENGTH_SHORT).show();
            }
        }

        // Para login con Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser Usuario = firebaseAuth.getCurrentUser();
                        if (Usuario != null) {
                            Intent intent = new Intent(registroActivity.this, logeocompletoActivity.class);
                            intent.putExtra("nombre", Usuario.getDisplayName());
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(registroActivity.this, "Falló autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtEdad.setText("");
        txtCorreo.setText("");
        txtContra.setText("");
    }
}