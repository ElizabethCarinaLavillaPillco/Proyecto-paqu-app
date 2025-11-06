package com.example.paqu;

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

public class logeocompletoActivity extends AppCompatActivity {
    Button btnContinuarRegistro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logeocompleto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView txtSaludo = findViewById(R.id.txtSaludo);
        btnContinuarRegistro = findViewById(R.id.btnContinuarRegistro);
        String nombre = getIntent().getStringExtra("nombre");
        txtSaludo.setText("¡Hola " + nombre + "! Creaste tu perfil con éxito");


        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(logeocompletoActivity.this, homeActivity.class);
            startActivity(intent);
            finish();
        }, 5000);

        btnContinuarRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(logeocompletoActivity.this, homeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}