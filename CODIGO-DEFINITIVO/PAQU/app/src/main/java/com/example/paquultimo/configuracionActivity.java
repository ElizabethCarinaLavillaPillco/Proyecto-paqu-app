package com.example.paquultimo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class configuracionActivity extends AppCompatActivity {

    Button btnPreferencias, btnPerfil, btnNotificaciones, btnAPrivacidad, btnCentroAyuda, btnSugerencias;
    TextView tvOk, tvPoliticaPrivacidad, tvTerminos, tvCerrarSesion;

    FirebaseAuth mAuth;
    Button btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //bonotes
        btnPreferencias = findViewById(R.id.btnPreferencias);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        btnAPrivacidad = findViewById(R.id.btnAPrivacidad);
        btnCentroAyuda = findViewById(R.id.btnCentroAyuda);
        btnSugerencias = findViewById(R.id.btnSugerencias);

        //textos q actuan cm botones xdd
        tvOk = findViewById(R.id.tvOk);
        tvCerrarSesion = findViewById(R.id.tvCerrarSesion);
        tvTerminos = findViewById(R.id.tvTerminos);
        tvPoliticaPrivacidad = findViewById(R.id.tvPoliticaPrivacidad);

        //acciones
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(configuracionActivity.this, perfilActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));


            }
        });

        //errar sesion
        mAuth = FirebaseAuth.getInstance();
        tvCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // Cierra la sesión
                Intent intent = new Intent(configuracionActivity.this, LoginActivity.class); // Reemplaza con tu actividad de login
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia la pila de actividades
                startActivity(intent);
                finish(); // Finaliza esta actividad
            }
        });


    }
}