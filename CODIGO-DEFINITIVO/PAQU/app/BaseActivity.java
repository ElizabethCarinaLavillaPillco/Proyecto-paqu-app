package com.example.paqu;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            bottomNav.setSelectedItemId(getSelectedNavItemId());
        }
    }

    protected abstract int getLayoutResourceId();
    protected abstract int getSelectedNavItemId();

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == getSelectedNavItemId()) {
                        return true; // Ya estamos en esta actividad
                    }

                    switch (id) {
                        case R.id.nav_home:
                            startActivity(new Intent(BaseActivity.this, homeActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            break;
                        case R.id.nav_dictionary:
                            startActivity(new Intent(BaseActivity.this, diccionarioActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            break;
                        case R.id.nav_profile:
                            startActivity(new Intent(BaseActivity.this, perfilActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            break;
                    }
                    overridePendingTransition(0, 0);
                    return true;
                }
            };
}