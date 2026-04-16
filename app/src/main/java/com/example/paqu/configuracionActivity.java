package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paqu.activities.InfoCuentaActivity;
import com.example.paqu.activities.InfoSoporteActivity;
import com.google.firebase.auth.FirebaseAuth;

public class configuracionActivity extends AppCompatActivity {

    // Botones comunes
    Button btnPreferencias, btnPerfil, btnNotificaciones, btnAPrivacidad,
            btnCentroAyuda, btnSugerencias, btnTamanioFuente, btnVolumen;

    // Botones exclusivos Admin
    Button btnGestionUsuarios, btnAsignarRoles, btnConfigApp;
    TextView tvSeccionAdmin;

    // Botones exclusivos Docente
    Button btnCrearLeccion, btnMisLecciones, btnGestionNiveles;
    TextView tvSeccionDocente;

    TextView tvOk, tvPoliticaPrivacidad, tvTerminos, tvCerrarSesion;

    FirebaseAuth mAuth;
    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener rol del usuario
        obtenerRolUsuario();

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        initViews();

        // Mostrar/ocultar opciones según rol
        configurarVistasPorRol();

        // Aplicar fuentes automáticamente
        aplicarFuentesAutomaticas();

        // Aplicar animaciones de entrada
        aplicarAnimacionesEntrada();

        // Configurar listeners
        setupListeners();
    }

    // ✅ NUEVO: Obtener rol desde SharedPreferences
    private void obtenerRolUsuario() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "usuario_comun");
    }

    // ✅ NUEVO: Configurar qué vistas mostrar según el rol
    private void configurarVistasPorRol() {
        switch (userRole) {
            case "administrador":
                // Mostrar sección Admin
                tvSeccionAdmin.setVisibility(View.VISIBLE);
                btnGestionUsuarios.setVisibility(View.VISIBLE);
                btnAsignarRoles.setVisibility(View.VISIBLE);
                btnConfigApp.setVisibility(View.VISIBLE);

                // Ajustar constraint de la sección Cuenta para que aparezca debajo de Admin
                ajustarConstraint(R.id.textView18, R.id.btnConfigApp);
                break;

            case "docente":
                // Mostrar sección Docente
                tvSeccionDocente.setVisibility(View.VISIBLE);
                btnCrearLeccion.setVisibility(View.VISIBLE);
                btnMisLecciones.setVisibility(View.VISIBLE);
                btnGestionNiveles.setVisibility(View.VISIBLE);

                // Ajustar constraint de la sección Cuenta para que aparezca debajo de Docente
                ajustarConstraint(R.id.textView18, R.id.btnGestionNiveles);
                break;

            case "usuario_comun":
            default:
                // Solo sección común, ajustar constraint al header
                ajustarConstraint(R.id.textView18, R.id.textView17);
                break;
        }
    }

    // ✅ NUEVO: Ajustar constraint dinámicamente
    private void ajustarConstraint(int viewId, int topToBottomId) {
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) findViewById(viewId).getLayoutParams();
        params.topToBottom = topToBottomId;
        findViewById(viewId).setLayoutParams(params);
    }

    private void initViews() {
        // Botones comunes
        btnPreferencias = findViewById(R.id.btnPreferencias);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        btnAPrivacidad = findViewById(R.id.btnAPrivacidad);
        btnCentroAyuda = findViewById(R.id.btnCentroAyuda);
        btnSugerencias = findViewById(R.id.btnSugerencias);
        btnTamanioFuente = findViewById(R.id.btnTamanioFuente);
        btnVolumen = findViewById(R.id.btnVolumen);

        // Admin
        tvSeccionAdmin = findViewById(R.id.tvSeccionAdmin);
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnAsignarRoles = findViewById(R.id.btnAsignarRoles);
        btnConfigApp = findViewById(R.id.btnConfigApp);

        // Docente
        tvSeccionDocente = findViewById(R.id.tvSeccionDocente);
        btnCrearLeccion = findViewById(R.id.btnCrearLeccion);
        btnMisLecciones = findViewById(R.id.btnMisLecciones);
        btnGestionNiveles = findViewById(R.id.btnGestionNiveles);

        // TextViews
        tvOk = findViewById(R.id.tvOk);
        tvCerrarSesion = findViewById(R.id.tvCerrarSesion);
        tvTerminos = findViewById(R.id.tvTerminos);
        tvPoliticaPrivacidad = findViewById(R.id.tvPoliticaPrivacidad);
    }

    private void setupListeners() {
        // OK - Volver
        tvOk.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, perfilActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });

        // Cerrar Sesión
        tvCerrarSesion.setOnClickListener(v -> {
            animarClick(v);
            mAuth.signOut();

            // Limpiar SharedPreferences del rol
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().remove("user_role").apply();

            Intent intent = new Intent(this, loginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Tamaño de Fuente
        btnTamanioFuente.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, TamanioFuenteActivity.class));
        });

        // Volumen
        btnVolumen.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, VolumenAjusteActivity.class));
        });

        // Perfil
        btnPerfil.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, perfilActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        });

        // Resto de botones comunes
        btnPreferencias.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, InfoCuentaActivity.class));
        });

        btnNotificaciones.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        btnAPrivacidad.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Ajustes de Privacidad", Toast.LENGTH_SHORT).show();
        });

        btnCentroAyuda.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, InfoSoporteActivity.class));
        });

        btnSugerencias.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Sugerencias", Toast.LENGTH_SHORT).show();
        });

        tvPoliticaPrivacidad.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Política de Privacidad", Toast.LENGTH_SHORT).show();
        });

        tvTerminos.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(this, "Términos y Condiciones", Toast.LENGTH_SHORT).show();
        });

        // ===== LISTENERS EXCLUSIVOS ADMIN =====
        if (userRole.equals("administrador")) {
            btnGestionUsuarios.setOnClickListener(v -> {
                animarClick(v);
                startActivity(new Intent(this, GestionUsuariosActivity.class));
            });

            btnAsignarRoles.setOnClickListener(v -> {
                animarClick(v);
                startActivity(new Intent(this, AsignarRolesActivity.class));
            });

            btnConfigApp.setOnClickListener(v -> {
                animarClick(v);
                startActivity(new Intent(this, ConfigAppActivity.class));
            });
        }

        // ===== LISTENERS EXCLUSIVOS DOCENTE =====
        if (userRole.equals("docente")) {
            btnCrearLeccion.setOnClickListener(v -> {
                animarClick(v);
                startActivity(new Intent(this, CrearLeccionActivity.class));
            });

            btnMisLecciones.setOnClickListener(v -> {
                animarClick(v);
                startActivity(new Intent(this, MisLeccionesActivity.class));
            });

            btnGestionNiveles.setOnClickListener(v -> {
                animarClick(v);
                startActivity(new Intent(this, GestionNivelesActivity.class));
            });
        }
    }

    // ============= ANIMACIONES =============

    private void aplicarAnimacionesEntrada() {
        View[] vistasComunes = {
                btnPreferencias, btnPerfil, btnNotificaciones,
                btnTamanioFuente, btnVolumen,
                btnAPrivacidad, btnCentroAyuda, btnSugerencias
        };

        for (int i = 0; i < vistasComunes.length; i++) {
            View vista = vistasComunes[i];
            if (vista.getVisibility() == View.VISIBLE) {
                vista.setAlpha(0f);
                vista.setTranslationX(-100f);

                vista.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(500)
                        .setStartDelay(i * 80)
                        .start();
            }
        }

        // Animar vistas de Admin si están visibles
        if (userRole.equals("administrador")) {
            View[] vistasAdmin = {tvSeccionAdmin, btnGestionUsuarios, btnAsignarRoles, btnConfigApp};
            for (View vista : vistasAdmin) {
                if (vista != null) {
                    vista.setAlpha(0f);
                    vista.animate().alpha(1f).setDuration(500).start();
                }
            }
        }

        // Animar vistas de Docente si están visibles
        if (userRole.equals("docente")) {
            View[] vistasDocente = {tvSeccionDocente, btnCrearLeccion, btnMisLecciones, btnGestionNiveles};
            for (View vista : vistasDocente) {
                if (vista != null) {
                    vista.setAlpha(0f);
                    vista.animate().alpha(1f).setDuration(500).start();
                }
            }
        }
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    // ============= FUENTES =============

    private void aplicarFuentesAutomaticas() {
        aplicarFuente(R.id.textView17, 20f);
        aplicarFuente(R.id.tvSeccionAdmin, 16f);
        aplicarFuente(R.id.tvSeccionDocente, 16f);
        aplicarFuente(R.id.textView18, 16f);
        aplicarFuente(R.id.text, 16f);
        aplicarFuente(R.id.text5, 16f);
        aplicarFuente(R.id.tvOk, 16f);
        aplicarFuente(R.id.tvCerrarSesion, 16f);
        aplicarFuente(R.id.tvPoliticaPrivacidad, 14f);
        aplicarFuente(R.id.tvTerminos, 14f);
        aplicarFuenteBotones();
    }

    private void aplicarFuenteBotones() {
        int[] botonesIds = {
                R.id.btnPreferencias, R.id.btnPerfil, R.id.btnNotificaciones,
                R.id.btnAPrivacidad, R.id.btnCentroAyuda, R.id.btnSugerencias,
                R.id.btnTamanioFuente, R.id.btnVolumen,
                // Admin
                R.id.btnGestionUsuarios, R.id.btnAsignarRoles, R.id.btnConfigApp,
                // Docente
                R.id.btnCrearLeccion, R.id.btnMisLecciones, R.id.btnGestionNiveles
        };

        for (int botonId : botonesIds) {
            Button boton = findViewById(botonId);
            if (boton != null && boton.getVisibility() == View.VISIBLE) {
                aplicarTamanioFuente(boton, 14f);
            }
        }
    }

    private void aplicarFuente(int textViewId, float tamanioBase) {
        TextView textView = findViewById(textViewId);
        if (textView != null && textView.getVisibility() == View.VISIBLE) {
            aplicarTamanioFuente(textView, tamanioBase);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarFuentesAutomaticas();
    }

    // Métodos estáticos (para uso global)
    public static float obtenerFactorFuente(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("configuracion_app", android.content.Context.MODE_PRIVATE);
        return prefs.getFloat("factor_fuente", 1.0f);
    }

    public static void aplicarTamanioFuente(TextView textView, float tamanioBase) {
        float factor = obtenerFactorFuente(textView.getContext());
        float nuevoTamanio = tamanioBase * factor;
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, nuevoTamanio);
    }
}