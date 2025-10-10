// BaseActivity.java
package com.example.paquultimo;

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
        // Eliminamos setContentView aquí, cada actividad hija lo manejará
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

    // En BaseActivity
    private final Map<Integer, Class<?>> navigationMap = new HashMap<Integer, Class<?>>() {{
        put(R.id.nav_home, homeActivity.class);
        put(R.id.nav_dictionary, diccionarioActivity.class);
        put(R.id.nav_minijuegos, MiniJuegosActivity.class);
        put(R.id.nav_profile, perfilActivity.class);
    }};
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == getSelectedNavItemId()) {
                        return true;
                    }

                    Class<?> targetActivity = navigationMap.get(id);
                    if (targetActivity != null) {
                        startActivity(new Intent(BaseActivity.this, targetActivity)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        overridePendingTransition(0, 0);
                    }
                    return true;
                }
            };
}