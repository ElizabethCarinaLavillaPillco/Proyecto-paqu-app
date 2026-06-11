package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class registroActivity extends AppCompatActivity {
    // Componentes de UI
    private EditText txtNombre, txtCorreo, txtContra;
    private Spinner spinnerDia, spinnerMes, spinnerAno;
    private Button btnIngresarApp, btnCerrar;
    private SignInButton btnGoogle;

    // TextViews de error para cada campo
    private TextView errorNombre, errorFecha, errorCorreo, errorContra;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    // Login con Google
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;

    // Login con Facebook
    private CallbackManager callbackManager;

    // Validación de email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicialización de vistas
        btnIngresarApp = findViewById(R.id.btnIngresarApp);
        btnCerrar = findViewById(R.id.btnCerrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtNombre = findViewById(R.id.txtNombre);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtContra = findViewById(R.id.txtContra);
        spinnerDia = findViewById(R.id.spinnerDia);
        spinnerMes = findViewById(R.id.spinnerMes);
        spinnerAno = findViewById(R.id.spinnerAno);

        // Inicializar TextViews de error
        errorNombre = findViewById(R.id.errorNombre);
        errorFecha = findViewById(R.id.errorFecha);
        errorCorreo = findViewById(R.id.errorCorreo);
        errorContra = findViewById(R.id.errorContra);

        // Inicialización de Firebase
        inicializarFirebase();
        firebaseAuth = FirebaseAuth.getInstance();

        // Configurar Spinners de fecha
        configurarSpinnersFecha();

        // Configuración de validación en tiempo real
        configurarValidacionTiempoReal();

        // Configuración de listeners
        configurarLoginGoogle();
        configurarLoginFacebook();

        btnIngresarApp.setOnClickListener(v -> registrarUsuario());

        btnCerrar.setOnClickListener(v -> {
            startActivity(new Intent(registroActivity.this, profileActivity.class));
            finish();
        });
    }

    /**
     * Configura los spinners para seleccionar día, mes y año
     */
    private void configurarSpinnersFecha() {
        Calendar calendario = Calendar.getInstance();
        int anoActual = calendario.get(Calendar.YEAR);

        // Configurar Spinner de Días (1-31)
        ArrayList<String> dias = new ArrayList<>();
        dias.add("Día");
        for (int i = 1; i <= 31; i++) {
            dias.add(String.format("%02d", i));
        }
        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dias);
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapterDias);

        // Configurar Spinner de Meses
        ArrayList<String> meses = new ArrayList<>();
        meses.add("Mes");
        String[] nombresMeses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        for (String mes : nombresMeses) {
            meses.add(mes);
        }
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, meses);
        adapterMeses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMes.setAdapter(adapterMeses);

        // Configurar Spinner de Años (desde 1950 hasta hace 8 años)
        ArrayList<String> anos = new ArrayList<>();
        anos.add("Año");
        int anoMinimo = anoActual - 100;
        int anoMaximo = anoActual - 8; // Mínimo 8 años
        for (int i = anoMaximo; i >= anoMinimo; i--) {
            anos.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapterAnos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, anos);
        adapterAnos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAno.setAdapter(adapterAnos);
    }

    /**
     * Configura validación en tiempo real mientras el usuario escribe
     */
    private void configurarValidacionTiempoReal() {

        txtNombre.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarNombre();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        txtCorreo.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarCorreo();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        txtContra.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarContraseña();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        android.widget.AdapterView.OnItemSelectedListener fechaListener =
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               android.view.View view,
                                               int position,
                                               long id) {
                        validarEdad();
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                };

        spinnerDia.setOnItemSelectedListener(fechaListener);
        spinnerMes.setOnItemSelectedListener(fechaListener);
        spinnerAno.setOnItemSelectedListener(fechaListener);
    }

    /**
     * Valida el nombre en tiempo real mientras escribe
     */
    private void validarNombre() {
        String nombre = txtNombre.getText().toString().trim();

        // Si está vacío, no mostrar error aún (solo si tiene texto incompleto)
        if (nombre.isEmpty()) {
            limpiarErrorNombre();
            marcarEditTextInvalido(txtNombre, false);
            return;
        }

        if (nombre.length() < 3) {
            mostrarErrorNombre("❌ Mínimo 3 caracteres");
            marcarEditTextInvalido(txtNombre, true);
            return;
        }

        // Si pasa validación, limpiar error
        marcarEditTextInvalido(txtNombre, false);
        limpiarErrorNombre();
    }

    /**
     * Valida el correo en tiempo real mientras escribe
     */
    private void validarCorreo() {
        String correo = txtCorreo.getText().toString().trim();

        // Si está vacío, no mostrar error aún
        if (correo.isEmpty()) {
            limpiarErrorCorreo();
            marcarEditTextInvalido(txtCorreo, false);
            return;
        }

        if (!validarFormatoCorreo(correo)) {
            mostrarErrorCorreo("❌ Correo inválido. Por favor ingresa un correo válido (ej: ejemplo@gmail.com)");
            marcarEditTextInvalido(txtCorreo, true);
            return;
        }

        // Si pasa validación, limpiar error
        marcarEditTextInvalido(txtCorreo, false);
        limpiarErrorCorreo();
    }

    /**
     * Valida la contraseña en tiempo real mientras escribe
     */
    private void validarContraseña() {
        String contra = txtContra.getText().toString().trim();

        // Si está vacío, no mostrar error aún
        if (contra.isEmpty()) {
            limpiarErrorContra();
            marcarEditTextInvalido(txtContra, false);
            return;
        }

        if (contra.length() < 6) {
            mostrarErrorContra("❌ Mínimo 6 caracteres (" + contra.length() + "/6)");
            marcarEditTextInvalido(txtContra, true);
            return;
        }

        // Si pasa validación, limpiar error
        marcarEditTextInvalido(txtContra, false);
        limpiarErrorContra();
    }

    /**
     * Valida la edad en tiempo real mientras selecciona
     */
    private void validarEdad() {
        String diaStr = spinnerDia.getSelectedItem().toString();
        String mesStr = spinnerMes.getSelectedItem().toString();
        String anoStr = spinnerAno.getSelectedItem().toString();

        // Si todavía no se ha completado la fecha,
        // quitar cualquier error y restaurar el color normal
        if (diaStr.equals("Día") || mesStr.equals("Mes") || anoStr.equals("Año")) {

            limpiarErrorFecha();

            // Restaurar color normal de TODOS los spinners
            marcarSpinnerInvalido(spinnerDia, false);
            marcarSpinnerInvalido(spinnerMes, false);
            marcarSpinnerInvalido(spinnerAno, false);

            return;
        }

        // Si la fecha está completa pero el usuario tiene menos de 8 años
        if (!validarEdad(diaStr, mesStr, anoStr)) {

            mostrarErrorFecha("❌ Debes tener 8 años o más");

            // Pintar de rojo TODOS los spinners
            marcarSpinnerInvalido(spinnerDia, true);
            marcarSpinnerInvalido(spinnerMes, true);
            marcarSpinnerInvalido(spinnerAno, true);

            return;
        }

        // Si la edad es válida, quitar error y volver al color normal
        limpiarErrorFecha();

        marcarSpinnerInvalido(spinnerDia, false);
        marcarSpinnerInvalido(spinnerMes, false);
        marcarSpinnerInvalido(spinnerAno, false);
    }
    /**
     * Valida que el usuario tenga 8 años o más
     */
    private boolean validarEdad(String dia, String mes, String ano) {
        try {
            int mesNumero = obtenerNumeroMes(mes);

            Calendar fechaNacimiento = Calendar.getInstance();
            fechaNacimiento.set(Integer.parseInt(ano), mesNumero - 1, Integer.parseInt(dia));

            Calendar fechaActual = Calendar.getInstance();

            // Calcular edad
            int edad = fechaActual.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR);

            // Verificar si ya pasó el cumpleaños este año
            if (fechaActual.get(Calendar.MONTH) < fechaNacimiento.get(Calendar.MONTH) ||
                    (fechaActual.get(Calendar.MONTH) == fechaNacimiento.get(Calendar.MONTH) &&
                            fechaActual.get(Calendar.DAY_OF_MONTH) < fechaNacimiento.get(Calendar.DAY_OF_MONTH))) {
                edad--;
            }

            return edad >= 8;
        } catch (Exception e) {
            Log.e("VALIDACION_EDAD", "Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convierte el nombre del mes a número
     */
    private int obtenerNumeroMes(String nombreMes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        for (int i = 0; i < meses.length; i++) {
            if (meses[i].equalsIgnoreCase(nombreMes)) {
                return i + 1;
            }
        }
        return 1; // Por defecto enero
    }

    /**
     * Valida todos los campos del formulario (validación final)
     */
    private String validarFormulario(String nombre, String dia, String mes, String ano, String correo, String contra) {
        // Validar nombre
        if (nombre.isEmpty()) {
            return "❌ Por favor ingresa tu nombre";
        }
        if (nombre.length() < 3) {
            return "❌ El nombre debe tener al menos 3 caracteres";
        }

        // Validar fecha de nacimiento
        if (dia.equals("Día") || mes.equals("Mes") || ano.equals("Año")) {
            return "❌ Por favor selecciona tu fecha de nacimiento completa (día, mes y año)";
        }

        // Validar que la edad sea mayor a 8 años
        if (!validarEdad(dia, mes, ano)) {
            return "❌ Debes tener 8 años o más para registrarte";
        }

        // Validar correo
        if (correo.isEmpty()) {
            return "❌ Por favor ingresa tu correo electrónico";
        }
        if (!validarFormatoCorreo(correo)) {
            return "❌ Por favor ingresa un correo válido (ej: ejemplo@gmail.com)";
        }

        // Validar contraseña
        if (contra.isEmpty()) {
            return "❌ Por favor ingresa una contraseña";
        }
        if (contra.length() < 6) {
            return "❌ La contraseña debe tener mínimo 6 caracteres";
        }

        return ""; // Sin errores
    }

    /**
     * Valida el formato del correo electrónico
     */
    private boolean validarFormatoCorreo(String correo) {
        return EMAIL_PATTERN.matcher(correo).matches();
    }

    /**
     * Marca un EditText como inválido (cambia el color de fondo)
     */
    private void marcarEditTextInvalido(EditText campo, boolean invalido) {
        if (campo == null) return;

        if (invalido) {
            campo.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#FFCDD2")
                    )
            );
        } else {
            campo.setBackgroundTintList(null); // Quita el color rojo
            campo.setBackgroundResource(R.drawable.edit_text_background);
        }
    }

    /**
     * Marca un Spinner como inválido
     */
    private void marcarSpinnerInvalido(Spinner spinner, boolean invalido) {
        if (spinner == null) return;

        if (invalido) {
            spinner.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#FFCDD2")
                    )
            );
        } else {
            spinner.setBackgroundTintList(null); // Quita el rojo
            spinner.setBackgroundResource(R.drawable.spinner_background);
        }
    }

    /**
     * Muestra error debajo del campo Nombre
     */
    private void mostrarErrorNombre(String mensaje) {
        errorNombre.setText(mensaje);
        errorNombre.setVisibility(TextView.VISIBLE);
    }

    /**
     * Limpia el error del campo Nombre
     */
    private void limpiarErrorNombre() {
        errorNombre.setText("");
        errorNombre.setVisibility(TextView.GONE);
    }

    /**
     * Muestra error debajo del campo Correo
     */
    private void mostrarErrorCorreo(String mensaje) {
        errorCorreo.setText(mensaje);
        errorCorreo.setVisibility(TextView.VISIBLE);
    }

    /**
     * Limpia el error del campo Correo
     */
    private void limpiarErrorCorreo() {
        errorCorreo.setText("");
        errorCorreo.setVisibility(TextView.GONE);
    }

    /**
     * Muestra error debajo del campo Contraseña
     */
    private void mostrarErrorContra(String mensaje) {
        errorContra.setText(mensaje);
        errorContra.setVisibility(TextView.VISIBLE);
    }

    /**
     * Limpia el error del campo Contraseña
     */
    private void limpiarErrorContra() {
        errorContra.setText("");
        errorContra.setVisibility(TextView.GONE);
    }

    /**
     * Muestra error debajo del campo Fecha
     */
    private void mostrarErrorFecha(String mensaje) {
        errorFecha.setText(mensaje);
        errorFecha.setVisibility(TextView.VISIBLE);
    }

    /**
     * Limpia el error del campo Fecha
     */
    private void limpiarErrorFecha() {
        errorFecha.setText("");
        errorFecha.setVisibility(TextView.GONE);
    }

    /**
     * Registra el usuario con validaciones finales
     */
    private void registrarUsuario() {
        String nombre = txtNombre.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String contra = txtContra.getText().toString().trim();

        // Obtener valores de los spinners
        String diaStr = spinnerDia.getSelectedItem().toString();
        String mesStr = spinnerMes.getSelectedItem().toString();
        String anoStr = spinnerAno.getSelectedItem().toString();

        // Validar todos los campos antes de intentar registrar
        validarNombre();
        validarCorreo();
        validarContraseña();
        validarEdad();

        // Validar campos principales
        String error = validarFormulario(nombre, diaStr, mesStr, anoStr, correo, contra);

        if (!error.isEmpty()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            return;
        }

        // Si todo es válido, proceder con el registro
        firebaseAuth.createUserWithEmailAndPassword(correo, contra)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar datos en la base de datos
                            guardarDatosUsuario(user.getUid(), nombre, correo, diaStr, mesStr, anoStr);

                            // Guardar también en Firebase Authentication
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            redirigirABienvenida(nombre);
                                        } else {
                                            Toast.makeText(registroActivity.this, "❌ Error al guardar nombre", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        mostrarErrorRegistro(task.getException());
                    }
                });
    }

    /**
     * Guarda los datos del usuario en la base de datos
     */
    private void guardarDatosUsuario(String uid, String nombre, String correo, String dia, String mes, String ano) {

        String fechaNacimiento = dia + "/" + mes + "/" + ano;

        Usuario usuario = new Usuario(nombre, correo, fechaNacimiento);

        Log.d("ROLE_TEST", "Role = " + usuario.role);

        databaseReference.child("Usuarios")
                .child(uid)
                .setValue(usuario)
                .addOnSuccessListener(unused -> {

                    databaseReference.child("Usuarios")
                            .child(uid)
                            .child("role")
                            .setValue("usuario_comun");

                    Toast.makeText(this, "Usuario guardado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Redirige a la actividad de bienvenida
     */
    private void redirigirABienvenida(String nombre) {
        Intent intent = new Intent(this, logeocompletoActivity.class);
        intent.putExtra("nombre", nombre);
        startActivity(intent);
        finish();
    }

    /**
     * Muestra errores específicos del registro
     */
    private void mostrarErrorRegistro(Exception exception) {
        String errorMessage = "❌ Error en registro";
        if (exception != null) {
            errorMessage = exception.getMessage();
            if (errorMessage.contains("email address is badly formatted")) {
                errorMessage = "❌ Formato de correo inválido";
            } else if (errorMessage.contains("password is weak")) {
                errorMessage = "❌ La contraseña debe tener al menos 6 caracteres";
            } else if (errorMessage.contains("email address is already in use")) {
                errorMessage = "❌ El correo ya está registrado";
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Inicializa Firebase
     */
    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance("https://paqu-df872-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();
    }

    /**
     * Configura el login con Google
     */
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

    /**
     * Configura el login con Facebook
     */
    private void configurarLoginFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(registroActivity.this, "✅ Login con Facebook exitoso", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(registroActivity.this, "❌ Login cancelado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(registroActivity.this, "❌ Error en Facebook Login", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "❌ Error en login con Google", Toast.LENGTH_SHORT).show();
            }
        }

        // Para login con Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Autentica con Firebase usando Google
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = firebaseAuth.getCurrentUser();
                        if (usuario != null) {
                            Intent intent = new Intent(registroActivity.this, logeocompletoActivity.class);
                            intent.putExtra("nombre", usuario.getDisplayName());
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(registroActivity.this, "❌ Falló autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}