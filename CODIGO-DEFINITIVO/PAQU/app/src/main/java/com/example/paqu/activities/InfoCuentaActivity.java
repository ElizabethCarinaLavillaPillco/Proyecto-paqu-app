package com.example.paqu.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paqu.R;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


public class InfoCuentaActivity extends AppCompatActivity {

    private TextView tvNombreUsuario, tvEmailUsuario, tvFechaRegistro;
    private TextView tvNivelActual, tvLeccionesCompletadas;
    private Button btnEditarPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_cuenta);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        tvEmailUsuario = findViewById(R.id.tvEmailUsuario);
        tvFechaRegistro = findViewById(R.id.tvFechaRegistro);
        tvNivelActual = findViewById(R.id.tvNivelActual);
        tvLeccionesCompletadas = findViewById(R.id.tvLeccionesCompletadas);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
    }

    private void loadUserData() {
        // Aquí integrarías con tu base de datos o SharedPreferences
        // Ejemplo con datos estáticos:
        tvNombreUsuario.setText("Usuario PAQU");
        tvEmailUsuario.setText("usuario@ejemplo.com");
        tvFechaRegistro.setText("Enero 2025");
        tvNivelActual.setText("Principiante");
        tvLeccionesCompletadas.setText("5/20");
    }

    private void setupClickListeners() {
        btnEditarPerfil.setOnClickListener(v -> {
            // Navegar a la actividad de edición de perfil
            // Intent intent = new Intent(this, EditarPerfilActivity.class);
            // startActivity(intent);
        });
    }
}