package com.example.paquultimo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.paquultimo.R;
import com.example.paquultimo.activities.TimeAttackActivity;
import com.example.paquultimo.homeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MiniJuegosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_juegos);


        // Configurar los cards de minijuegos
        setupGameCards();
    }

    private void setupGameCards() {
        CardView cardMemoria = findViewById(R.id.cardMemoria);
        CardView cardContrarreloj = findViewById(R.id.cardContrarreloj);

        // Juego de Memoria
        cardMemoria.setOnClickListener(v -> {
            Toast.makeText(this, "🎮 Sección en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Juego Contra el Reloj
        cardContrarreloj.setOnClickListener(v -> {
            Intent intent = new Intent(this, TimeAttackActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_minijuegos;
    }
}