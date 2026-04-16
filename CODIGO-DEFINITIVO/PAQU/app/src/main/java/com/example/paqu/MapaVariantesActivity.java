package com.example.paqu;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.adapters.VariantesListAdapter;
import com.example.paqu.managers.VariantesQuechuaManager;
import com.example.paqu.models.VarianteQuechua;

/**
 * Mapa de Variantes del Quechua
 * Actividad independiente SIN bottom navigation
 * Se accede desde la bandera de Cusco en homeActivity
 */
public class MapaVariantesActivity extends AppCompatActivity {

    private RecyclerView rvVariantes;
    private VariantesListAdapter adapter;
    private VariantesQuechuaManager variantesManager;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_variantes);

        initViews();
        initManager();
        setupRecyclerView();
        setupBackButton();
    }

    private void initViews() {
        rvVariantes = findViewById(R.id.rvVariantes);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initManager() {
        variantesManager = VariantesQuechuaManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new VariantesListAdapter(
                variantesManager.getVariantes(),
                this,
                variante -> {
                    // Cuando se hace click en una variante
                    Toast.makeText(this,
                            variante.getNombre() + "\n" +
                                    variante.getHablantesFormateado() + " hablantes\n" +
                                    variante.getEjemploPalabra() + " = " + variante.getEjemploTraduccion(),
                            Toast.LENGTH_LONG).show();
                }
        );

        rvVariantes.setLayoutManager(new LinearLayoutManager(this));
        rvVariantes.setAdapter(adapter);
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Volver a homeActivity
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}