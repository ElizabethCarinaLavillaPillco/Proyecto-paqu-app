package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            bottomNav.setSelectedItemId(getSelectedNavItemId());
        }
    }

    protected abstract int getSelectedNavItemId();

    // ✅ MAPA DE NAVEGACIÓN ACTUALIZADO
    private final Map<Integer, Class<?>> navigationMap = new HashMap<Integer, Class<?>>() {{
        put(R.id.nav_home, homeActivity.class);
        put(R.id.nav_dictionary, HerramientasActivity.class);
        put(R.id.nav_stats, EstadisticasActivity.class);
        put(R.id.nav_Minijuegos, MiniJuegosActivity.class);
        put(R.id.nav_profile, perfilActivity.class);

    }};

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    // Si ya estamos en esta activity, no hacer nada
                    if (id == getSelectedNavItemId()) {
                        return true;
                    }

                    Class<?> targetActivity = navigationMap.get(id);
                    if (targetActivity != null) {
                        Intent intent = new Intent(BaseActivity.this, targetActivity);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                    return true;
                }
            };
}