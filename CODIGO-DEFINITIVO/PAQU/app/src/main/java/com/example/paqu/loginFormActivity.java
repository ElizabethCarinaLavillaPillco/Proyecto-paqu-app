package com.example.paqu;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginFormActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnIngresar, btnCrear;
    private com.google.android.gms.common.SignInButton btnGoogle;
    private TextView tvRecuperarPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Configurar Google Sign-In
        configureGoogleSignIn();

        // Verificar si ya hay un usuario logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            startActivity(new Intent(this, homeActivity.class));
            finish();
            return;
        }

        // Inicializar vistas
        etEmail = findViewById(R.id.tvEmail);
        etPassword = findViewById(R.id.tvPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnCrear = findViewById(R.id.btnCrear);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRecuperarPassword = findViewById(R.id.tvRecuperarPassword);

        // Configurar listeners
        btnIngresar.setOnClickListener(v -> loginUser());

        btnCrear.setOnClickListener(v -> {
            startActivity(new Intent(loginFormActivity.this, registroActivity.class));
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Listener para recuperar contraseña
        tvRecuperarPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarPassword();
            }
        });
    }

    // MÉTODO: Recuperar contraseña con diseño degradado
    // MÉTODO: Recuperar contraseña con diseño degradado
    private void recuperarPassword() {
        // Crear el diálogo personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        if (etEmail.getText() != null && !etEmail.getText().toString().isEmpty()) {
            etEmailRecuperar.setText(etEmail.getText().toString());
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
        AlertDialog dialog = builder.create();

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

    // MÉTODO: Enviar email de recuperación con ProgressDialog personalizado
    private void enviarEmailRecuperacion(String email) {
        // Crear diálogo de progreso personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        AlertDialog progressDialog = builder.create();

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        progressDialog.show();

        // Enviar email de recuperación
        mAuth.sendPasswordResetEmail(email)
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

    // MÉTODO: Mostrar diálogo de éxito hermoso
    private void mostrarDialogoExito() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // MÉTODO: Mostrar diálogo de error hermoso
    private void mostrarDialogoError(Exception exception) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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

        // Botón Reintentar con degradado
        Button btnReintentar = new Button(this);
        btnReintentar.setText("Entendido");
        btnReintentar.setTextColor(Color.WHITE);
        btnReintentar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        btnReintentar.setTypeface(null, Typeface.BOLD);
        btnReintentar.setPadding(80, 30, 80, 30);

        GradientDrawable btnGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#ee0979"),
                        Color.parseColor("#ff6a00")
                }
        );
        btnGradient.setCornerRadius(70f);
        btnReintentar.setBackground(btnGradient);
        btnReintentar.setElevation(12f);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
        );
        btnReintentar.setLayoutParams(btnParams);
        contentLayout.addView(btnReintentar);

        mainLayout.addView(contentLayout);

        builder.setView(mainLayout);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnReintentar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // MÉTODO: Obtener mensaje de error personalizado
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

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Error en inicio de sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario autenticado con Google, ahora verificar si está registrado
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkIfUserIsRegistered(user);
                        }
                    } else {
                        Toast.makeText(this, "Error en autenticación con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método: Verificar si el usuario está registrado en Firebase Database
    private void checkIfUserIsRegistered(FirebaseUser user) {
        String userId = user.getUid();

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // ✅ Usuario registrado - permitir acceso
                    Toast.makeText(loginFormActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(loginFormActivity.this, homeActivity.class));
                    finish();
                } else {
                    // ❌ Usuario NO registrado - cerrar sesión y mostrar mensaje
                    mAuth.signOut();
                    googleSignInClient.signOut();
                    Toast.makeText(loginFormActivity.this,
                            "Usuario no registrado. Por favor, regístrate primero.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(loginFormActivity.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                googleSignInClient.signOut();
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        // Validaciones...
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

        // Mostrar loading
        btnIngresar.setEnabled(false);
        btnIngresar.setText("CARGANDO...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("INGRESAR");

                    if (task.isSuccessful()) {
                        // Para login normal, asumimos que el usuario está registrado
                        Intent i = new Intent(loginFormActivity.this, homeActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(loginFormActivity.this,
                                "Credenciales incorrectas",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            startActivity(new Intent(this, homeActivity.class));
            finish();
        }
    }
}