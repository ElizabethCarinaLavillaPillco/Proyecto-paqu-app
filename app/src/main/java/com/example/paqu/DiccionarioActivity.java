package com.example.paqu;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiccionarioActivity extends AppCompatActivity {

    private static final String TAG = "DiccionarioActivity";

    // UI Components
    ImageView btnBack;
    EditText etBuscar;
    ChipGroup chipGroupCategorias;
    RecyclerView rvPalabras;
    LinearLayout tvNoResultados;
    LottieAnimationView lottieSearch;

    // Data - 🔹 IMPORTANTE: Inicializar UNA SOLA VEZ
    private List<PalabraDiccionario> todasLasPalabras;
    private List<PalabraDiccionario> palabrasFiltradas;
    private DiccionarioAdapter adapter;
    private String categoriaSeleccionada = "Todas";

    // Firebase
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diccionario);

        // 🔹 Inicializar Firebase
        dbRef = FirebaseDatabase.getInstance().getReference();

        // 🔹 Inicializar listas UNA SOLA VEZ
        todasLasPalabras = new ArrayList<>();
        palabrasFiltradas = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();
        animacionEntrada();

        // 🔹 Cargar palabras desde Firebase
        cargarPalabrasDesdeFirebase();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etBuscar = findViewById(R.id.etBuscar);
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias);
        rvPalabras = findViewById(R.id.rvPalabras);
        tvNoResultados = findViewById(R.id.tvNoResultados);
        lottieSearch = findViewById(R.id.lottieSearch);
    }

    // 🔹 MÉTODO CORREGIDO: Cargar palabras desde Firebase
    private void cargarPalabrasDesdeFirebase() {
        dbRef.child("diccionario")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // 🔹 LIMPIAR las listas existentes (NO crear nuevas)
                        todasLasPalabras.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String quechua = ds.child("quechua").getValue(String.class);
                            String espanol = ds.child("espanol").getValue(String.class);
                            String categoria = ds.child("categoria").getValue(String.class);
                            String pronunciacion = ds.child("pronunciacion").getValue(String.class);

                            if (quechua != null && !quechua.isEmpty()) {
                                todasLasPalabras.add(new PalabraDiccionario(
                                        quechua,
                                        espanol != null ? espanol : "",
                                        categoria != null ? categoria : "Otra",
                                        pronunciacion != null ? pronunciacion : quechua.toLowerCase()
                                ));
                            }
                        }

                        // Ordenar alfabéticamente
                        todasLasPalabras.sort((a, b) -> a.quechua.compareToIgnoreCase(b.quechua));

                        Log.d(TAG, "✅ Cargadas " + todasLasPalabras.size() + " palabras desde Firebase");

                        // 🔹 Actualizar el filtro con la categoría actual
                        filtrarPalabras(etBuscar.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Error al cargar palabras: " + error.getMessage());
                        Toast.makeText(DiccionarioActivity.this,
                                "Error al cargar el diccionario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 MÉTODO CORREGIDO: Setup RecyclerView (sin crear nuevas listas)
    private void setupRecyclerView() {
        adapter = new DiccionarioAdapter(palabrasFiltradas, this);
        rvPalabras.setLayoutManager(new LinearLayoutManager(this));
        rvPalabras.setAdapter(adapter);
    }

    // 🔹 AGREGA ESTOS MÉTODOS al final de la clase:
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.onDestroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar audio si el usuario sale de la pantalla
        if (adapter != null) {
            adapter.onDestroy();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            animarClick(v);
            finish();
        });

        // Búsqueda
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPalabras(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filtros de categoría
        for (int i = 0; i < chipGroupCategorias.getChildCount(); i++) {
            View child = chipGroupCategorias.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    categoriaSeleccionada = chip.getText().toString();
                    filtrarPalabras(etBuscar.getText().toString());
                    animarChip(chip);
                });
            }
        }
    }

    // 🔹 MÉTODO CORREGIDO: Filtrar palabras
    private void filtrarPalabras(String query) {
        // 🔹 LIMPIAR la lista filtrada (NO crear nueva)
        palabrasFiltradas.clear();

        for (PalabraDiccionario palabra : todasLasPalabras) {
            boolean matchQuery = query.isEmpty() ||
                    palabra.quechua.toLowerCase().contains(query.toLowerCase()) ||
                    palabra.espanol.toLowerCase().contains(query.toLowerCase());

            boolean matchCategoria = categoriaSeleccionada.equals("Todas") ||
                    palabra.categoria.equals(categoriaSeleccionada);

            if (matchQuery && matchCategoria) {
                palabrasFiltradas.add(palabra);
            }
        }

        Log.d(TAG, "🔍 Filtradas: " + palabrasFiltradas.size() + " palabras (query: '" + query + "', categoría: '" + categoriaSeleccionada + "')");

        // 🔹 Notificar al adapter
        adapter.notifyDataSetChanged();

        // Mostrar mensaje si no hay resultados
        if (palabrasFiltradas.isEmpty()) {
            tvNoResultados.setVisibility(View.VISIBLE);
            rvPalabras.setVisibility(View.GONE);
            if (lottieSearch != null) {
                lottieSearch.playAnimation();
            }
        } else {
            tvNoResultados.setVisibility(View.GONE);
            rvPalabras.setVisibility(View.VISIBLE);
            if (lottieSearch != null) {
                lottieSearch.pauseAnimation();
            }
        }
    }


    // ============= ANIMACIONES =============

    private void animacionEntrada() {
        View[] vistas = {etBuscar, chipGroupCategorias, rvPalabras};

        for (int i = 0; i < vistas.length; i++) {
            View vista = vistas[i];
            vista.setAlpha(0f);
            vista.setTranslationY(50f);

            vista.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(i * 100)
                    .setInterpolator(new BounceInterpolator())
                    .start();
        }
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
    }

    private void animarChip(View chip) {
        ObjectAnimator.ofFloat(chip, "elevation", 2f, 8f, 2f)
                .setDuration(300)
                .start();
    }

    // Clase interna para datos
    public static class PalabraDiccionario {
        public String quechua;
        public String espanol;
        public String categoria;
        public String pronunciacion;

        public PalabraDiccionario(String quechua, String espanol, String categoria, String pronunciacion) {
            this.quechua = quechua;
            this.espanol = espanol;
            this.categoria = categoria;
            this.pronunciacion = pronunciacion;
        }
    }
}