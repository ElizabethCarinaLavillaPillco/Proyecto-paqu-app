package com.example.paqu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
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

        // Referencias UI (mismos IDs que tu XML)
        tvEmail = findViewById(R.id.tvEmail);
        tvPassword = findViewById(R.id.tvPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnCrear = findViewById(R.id.btnCrear);
        tvRecuperarPassword = findViewById(R.id.tvRecuperarPassword);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Configurar inicio de sesión con Google
        configurarLoginGoogle();

        // Botón normal: iniciar sesión con correo y contraseña
        btnIngresar.setOnClickListener(v -> loginConCorreo());

        // Botón: ir a registro
        btnCrear.setOnClickListener(v -> {
            Intent intent = new Intent(loginFormActivity.this, registroActivity.class);
            startActivity(intent);
            finish();
        });

        // Recuperar contraseña con diseño hermoso
        tvRecuperarPassword.setOnClickListener(v -> recuperarPassword());
    }

    // -------------------------------
    // 🔹 CONFIGURAR LOGIN GOOGLE
    // -------------------------------
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

    // -------------------------------
    // 🔹 LOGIN CON CORREO Y CONTRASEÑA
    // -------------------------------
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

    // -------------------------------
    // 🔹 RESULTADO LOGIN GOOGLE
    // -------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d("GOOGLE_LOGIN", "Resultado del intento de Google recibido");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e("GOOGLE_LOGIN", "Error al iniciar sesión con Google: " + e.getMessage());
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // -------------------------------
    // 🔹 AUTENTICAR CON FIREBASE
    // -------------------------------
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("GOOGLE_LOGIN", "Autenticando con Firebase...");
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
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

    // -------------------------------
    // 🔹 REDIRECCIÓN A HOMEACTIVITY
    // -------------------------------
    private void redirigirAHome() {
        Intent intent = new Intent(loginFormActivity.this, homeActivity.class);
        startActivity(intent);
        finish();
    }

    // -------------------------------
    // 🔹 RECUPERAR CONTRASEÑA CON DISEÑO HERMOSO
    // -------------------------------
    private void recuperarPassword() {
        // Crear el diálogo personalizado
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // Crear el layout principal
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);

        // Crear drawable con esquinas redondeadas
        GradientDrawable dialogBackground = new GradientDrawable();
        dialogBackground.setCornerRadius(60f);
        dialogBackground.setColor(Color.WHITE);
        mainLayout.setBackground(dialogBackground);

        // ===== HEADER CON DEGRADADO =====
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setPadding(80, 80, 80, 80);
        headerLayout.setGravity(Gravity.CENTER);

        // Crear degradado para el header
        GradientDrawable headerGradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.parseColor("#667eea"),  // Púrpura claro
                        Color.parseColor("#764ba2"),  // Púrpura medio
                        Color.parseColor("#f093fb")   // Rosa suave
                }
        );
        headerGradient.setCornerRadii(new float[]{60f, 60f, 60f, 60f, 0f, 0f, 0f, 0f});
        headerLayout.setBackground(headerGradient);

        // Icono del candado
        ImageView iconLock = new ImageView(this);
        iconLock.setImageResource(android.R.drawable.ic_lock_idle_lock);
        iconLock.setColorFilter(Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(150, 150);
        iconParams.gravity = Gravity.CENTER;
        iconLock.setLayoutParams(iconParams);
        headerLayout.addView(iconLock);

        // Título del header
        TextView headerTitle = new TextView(this);
        headerTitle.setText("Recuperar Contraseña");
        headerTitle.setTextColor(Color.WHITE);
        headerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        headerTitle.setTypeface(null, Typeface.BOLD);
        headerTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = 30;
        headerTitle.setLayoutParams(titleParams);
        headerLayout.addView(headerTitle);

        mainLayout.addView(headerLayout);

        // ===== CONTENIDO =====
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(70, 60, 70, 60);

        // Texto descriptivo
        TextView description = new TextView(this);
        description.setText("Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña");
        description.setTextColor(Color.parseColor("#616161"));
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        description.setLineSpacing(1.3f, 1.0f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.bottomMargin = 50;
        description.setLayoutParams(descParams);
        contentLayout.addView(description);

        // Campo de email con diseño
        LinearLayout emailContainer = new LinearLayout(this);
        emailContainer.setOrientation(LinearLayout.HORIZONTAL);
        emailContainer.setPadding(40, 30, 40, 30);
        emailContainer.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable emailBackground = new GradientDrawable();
        emailBackground.setCornerRadius(35f);
        emailBackground.setStroke(4, Color.parseColor("#667eea"));
        emailBackground.setColor(Color.WHITE);
        emailContainer.setBackground(emailBackground);

        // Icono de email
        ImageView emailIcon = new ImageView(this);
        emailIcon.setImageResource(android.R.drawable.ic_dialog_email);
        emailIcon.setColorFilter(Color.parseColor("#667eea"));
        LinearLayout.LayoutParams emailIconParams = new LinearLayout.LayoutParams(60, 60);
        emailIconParams.rightMargin = 20;
        emailIcon.setLayoutParams(emailIconParams);
        emailContainer.addView(emailIcon);

        // EditText para el email
        final EditText etEmailRecuperar = new EditText(this);
        etEmailRecuperar.setHint("ejemplo@correo.com");
        etEmailRecuperar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        etEmailRecuperar.setTextColor(Color.BLACK); // ✅ Color negro para el texto
        etEmailRecuperar.setHintTextColor(Color.parseColor("#999999")); // Color gris para el hint
        etEmailRecuperar.setBackgroundColor(Color.TRANSPARENT);
        etEmailRecuperar.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        etEmailRecuperar.setLayoutParams(etParams);

        // Si ya hay un email, sugerirlo
        if (tvEmail.getText() != null && !tvEmail.getText().toString().isEmpty()) {
            etEmailRecuperar.setText(tvEmail.getText().toString());
        }

        emailContainer.addView(etEmailRecuperar);

        LinearLayout.LayoutParams emailContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emailContainerParams.bottomMargin = 60;
        emailContainer.setLayoutParams(emailContainerParams);
        contentLayout.addView(emailContainer);

        // ===== BOTONES =====
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.END);

        // Botón Cancelar
        Button btnCancelar = new Button(this);
        btnCancelar.setText("Cancelar");
        btnCancelar.setTextColor(Color.parseColor("#616161"));
        btnCancelar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btnCancelar.setTypeface(null, Typeface.BOLD);
        btnCancelar.setBackgroundColor(Color.TRANSPARENT);
        btnCancelar.setPadding(50, 30, 50, 30);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                140
        );
        cancelParams.rightMargin = 20;
        btnCancelar.setLayoutParams(cancelParams);

        // Botón Enviar con degradado
        Button btnEnviar = new Button(this);
        btnEnviar.setText("Enviar Enlace");
        btnEnviar.setTextColor(Color.WHITE);
        btnEnviar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btnEnviar.setTypeface(null, Typeface.BOLD);
        btnEnviar.setPadding(60, 30, 60, 30);

        GradientDrawable buttonGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#667eea"),
                        Color.parseColor("#764ba2")
                }
        );
        buttonGradient.setCornerRadius(70f);
        btnEnviar.setBackground(buttonGradient);
        btnEnviar.setElevation(12f);

        LinearLayout.LayoutParams enviarParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                140
        );
        btnEnviar.setLayoutParams(enviarParams);

        buttonsLayout.addView(btnCancelar);
        buttonsLayout.addView(btnEnviar);
        contentLayout.addView(buttonsLayout);

        mainLayout.addView(contentLayout);

        // Configurar el diálogo
        builder.setView(mainLayout);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Hacer el fondo transparente
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Listeners de los botones
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnEnviar.setOnClickListener(v -> {
            String email = etEmailRecuperar.getText().toString().trim();
            if (!email.isEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    dialog.dismiss();
                    enviarEmailRecuperacion(email);
                } else {
                    etEmailRecuperar.setError("Ingresa un email válido");
                    etEmailRecuperar.requestFocus();
                }
            } else {
                etEmailRecuperar.setError("Por favor ingresa un email");
                etEmailRecuperar.requestFocus();
            }
        });

        dialog.show();
    }

    // -------------------------------
    // 🔹 ENVIAR EMAIL DE RECUPERACIÓN
    // -------------------------------
    private void enviarEmailRecuperacion(String email) {
        // Crear diálogo de progreso personalizado
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // Layout principal
        LinearLayout progressLayout = new LinearLayout(this);
        progressLayout.setOrientation(LinearLayout.VERTICAL);
        progressLayout.setPadding(80, 80, 80, 80);
        progressLayout.setGravity(Gravity.CENTER);

        // Fondo con degradado y esquinas redondeadas
        GradientDrawable progressBackground = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.parseColor("#667eea"),
                        Color.parseColor("#764ba2"),
                        Color.parseColor("#f093fb")
                }
        );
        progressBackground.setCornerRadius(50f);
        progressLayout.setBackground(progressBackground);

        // ProgressBar (spinner circular)
        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
        progressBar.setIndeterminate(true);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(120, 120);
        progressParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressParams);
        progressLayout.addView(progressBar);

        // Texto de carga
        TextView loadingText = new TextView(this);
        loadingText.setText("Enviando enlace...");
        loadingText.setTextColor(Color.WHITE);
        loadingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        loadingText.setTypeface(null, Typeface.BOLD);
        loadingText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = 40;
        loadingText.setLayoutParams(textParams);
        progressLayout.addView(loadingText);

        builder.setView(progressLayout);
        builder.setCancelable(false);
        androidx.appcompat.app.AlertDialog progressDialog = builder.create();

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        progressDialog.show();

        // Enviar email de recuperación
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            mostrarDialogoExito();
                        } else {
                            mostrarDialogoError(task.getException());
                        }
                    }
                });
    }

    // -------------------------------
    // 🔹 MOSTRAR DIÁLOGO DE ÉXITO
    // -------------------------------
    private void mostrarDialogoExito() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // Layout principal
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);

        GradientDrawable dialogBg = new GradientDrawable();
        dialogBg.setCornerRadius(60f);
        dialogBg.setColor(Color.WHITE);
        mainLayout.setBackground(dialogBg);

        // Header con degradado verde (éxito)
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setPadding(80, 80, 80, 80);
        headerLayout.setGravity(Gravity.CENTER);

        GradientDrawable headerGradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.parseColor("#11998e"),  // Verde azulado
                        Color.parseColor("#38ef7d"),  // Verde brillante
                        Color.parseColor("#b2fefa")   // Cyan claro
                }
        );
        headerGradient.setCornerRadii(new float[]{60f, 60f, 60f, 60f, 0f, 0f, 0f, 0f});
        headerLayout.setBackground(headerGradient);

        // Icono de éxito (checkmark)
        TextView checkIcon = new TextView(this);
        checkIcon.setText("✓");
        checkIcon.setTextColor(Color.WHITE);
        checkIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80);
        checkIcon.setTypeface(null, Typeface.BOLD);
        checkIcon.setGravity(Gravity.CENTER);

        // Círculo de fondo para el check
        GradientDrawable checkCircle = new GradientDrawable();
        checkCircle.setShape(GradientDrawable.OVAL);
        checkCircle.setColor(Color.parseColor("#22ffffff"));
        checkCircle.setSize(180, 180);
        checkIcon.setBackground(checkCircle);
        checkIcon.setPadding(0, 20, 0, 0);

        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(180, 180);
        checkParams.gravity = Gravity.CENTER;
        checkIcon.setLayoutParams(checkParams);
        headerLayout.addView(checkIcon);

        // Título
        TextView title = new TextView(this);
        title.setText("¡Email Enviado!");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = 30;
        title.setLayoutParams(titleParams);
        headerLayout.addView(title);

        mainLayout.addView(headerLayout);

        // Contenido
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(70, 60, 70, 60);

        TextView message = new TextView(this);
        message.setText("Hemos enviado un enlace de recuperación a tu correo. Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contraseña.");
        message.setTextColor(Color.parseColor("#616161"));
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        message.setLineSpacing(1.4f, 1.0f);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.bottomMargin = 50;
        message.setLayoutParams(msgParams);
        contentLayout.addView(message);

        // Botón Entendido con degradado
        Button btnEntendido = new Button(this);
        btnEntendido.setText("Entendido");
        btnEntendido.setTextColor(Color.WHITE);
        btnEntendido.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        btnEntendido.setTypeface(null, Typeface.BOLD);
        btnEntendido.setPadding(80, 30, 80, 30);

        GradientDrawable btnGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#11998e"),
                        Color.parseColor("#38ef7d")
                }
        );
        btnGradient.setCornerRadius(70f);
        btnEntendido.setBackground(btnGradient);
        btnEntendido.setElevation(12f);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
        );
        btnEntendido.setLayoutParams(btnParams);
        contentLayout.addView(btnEntendido);

        mainLayout.addView(contentLayout);

        builder.setView(mainLayout);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // -------------------------------
    // 🔹 MOSTRAR DIÁLOGO DE ERROR
    // -------------------------------
    private void mostrarDialogoError(Exception exception) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // Layout principal
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);

        GradientDrawable dialogBg = new GradientDrawable();
        dialogBg.setCornerRadius(60f);
        dialogBg.setColor(Color.WHITE);
        mainLayout.setBackground(dialogBg);

        // Header con degradado rojo (error)
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setPadding(80, 80, 80, 80);
        headerLayout.setGravity(Gravity.CENTER);

        GradientDrawable headerGradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.parseColor("#ee0979"),  // Rosa fuerte
                        Color.parseColor("#ff6a00"),  // Naranja
                        Color.parseColor("#ffd89b")   // Amarillo suave
                }
        );
        headerGradient.setCornerRadii(new float[]{60f, 60f, 60f, 60f, 0f, 0f, 0f, 0f});
        headerLayout.setBackground(headerGradient);

        // Icono de error
        TextView errorIcon = new TextView(this);
        errorIcon.setText("✕");
        errorIcon.setTextColor(Color.WHITE);
        errorIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80);
        errorIcon.setTypeface(null, Typeface.BOLD);
        errorIcon.setGravity(Gravity.CENTER);

        GradientDrawable errorCircle = new GradientDrawable();
        errorCircle.setShape(GradientDrawable.OVAL);
        errorCircle.setColor(Color.parseColor("#22ffffff"));
        errorCircle.setSize(180, 180);
        errorIcon.setBackground(errorCircle);
        errorIcon.setPadding(0, 10, 0, 0);

        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(180, 180);
        errorParams.gravity = Gravity.CENTER;
        errorIcon.setLayoutParams(errorParams);
        headerLayout.addView(errorIcon);

        // Título
        TextView title = new TextView(this);
        title.setText("Error al Enviar");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = 30;
        title.setLayoutParams(titleParams);
        headerLayout.addView(title);

        mainLayout.addView(headerLayout);

        // Contenido
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(70, 60, 70, 60);

        TextView message = new TextView(this);
        message.setText(obtenerMensajeError(exception));
        message.setTextColor(Color.parseColor("#616161"));
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        message.setLineSpacing(1.4f, 1.0f);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.bottomMargin = 50;
        message.setLayoutParams(msgParams);
        contentLayout.addView(message);

        // Botón Entendido con degradado
        Button btnEntendido = new Button(this);
        btnEntendido.setText("Entendido");
        btnEntendido.setTextColor(Color.WHITE);
        btnEntendido.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        btnEntendido.setTypeface(null, Typeface.BOLD);
        btnEntendido.setPadding(80, 30, 80, 30);

        GradientDrawable btnGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#ee0979"),
                        Color.parseColor("#ff6a00")
                }
        );
        btnGradient.setCornerRadius(70f);
        btnEntendido.setBackground(btnGradient);
        btnEntendido.setElevation(12f);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
        );
        btnEntendido.setLayoutParams(btnParams);
        contentLayout.addView(btnEntendido);

        mainLayout.addView(contentLayout);

        builder.setView(mainLayout);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // -------------------------------
    // 🔹 OBTENER MENSAJE DE ERROR PERSONALIZADO
    // -------------------------------
    private String obtenerMensajeError(Exception exception) {
        if (exception == null) {
            return "Error desconocido. Por favor intenta nuevamente.";
        }

        String errorMessage = exception.getMessage();

        // Verificar si es error de red
        if (errorMessage != null &&
                (errorMessage.toLowerCase().contains("network") ||
                        errorMessage.toLowerCase().contains("connection") ||
                        errorMessage.toLowerCase().contains("internet") ||
                        errorMessage.toLowerCase().contains("unreachable"))) {
            return "Error de conexión. Verifica tu internet e intenta nuevamente.";
        }

        // Verificar otros errores comunes
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "No existe una cuenta registrada con este correo electrónico.";
        }

        if (errorMessage != null && errorMessage.toLowerCase().contains("invalid email")) {
            return "El formato del correo electrónico no es válido.";
        }

        if (errorMessage != null && errorMessage.toLowerCase().contains("user not found")) {
            return "No se encontró ningún usuario con este correo.";
        }

        // Error genérico
        return "No se pudo enviar el enlace. Por favor verifica el correo e intenta nuevamente.";
    }
}