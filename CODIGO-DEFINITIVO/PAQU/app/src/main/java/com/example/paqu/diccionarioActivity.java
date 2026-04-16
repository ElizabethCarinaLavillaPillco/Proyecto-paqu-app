package com.example.paqu;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class DiccionarioActivity extends AppCompatActivity {

    // UI Components
    ImageView btnBack;
    EditText etBuscar;
    ChipGroup chipGroupCategorias;
    RecyclerView rvPalabras;
    LinearLayout tvNoResultados;
    LottieAnimationView lottieSearch;

    // Data
    private List<PalabraDiccionario> todasLasPalabras;
    private List<PalabraDiccionario> palabrasFiltradas;
    private DiccionarioAdapter adapter;
    private String categoriaSeleccionada = "Todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diccionario);

        initViews();
        initData();
        setupRecyclerView();
        setupListeners();
        animacionEntrada();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etBuscar = findViewById(R.id.etBuscar);
        chipGroupCategorias = findViewById(R.id.chipGroupCategorias);
        rvPalabras = findViewById(R.id.rvPalabras);
        tvNoResultados = findViewById(R.id.tvNoResultados);
        lottieSearch = findViewById(R.id.lottieSearch);
    }

    private void initData() {
        todasLasPalabras = new ArrayList<>();

        // Saludos y Expresiones Comunes
        todasLasPalabras.add(new PalabraDiccionario("Allinllachu", "¿Cómo estás?", "Saludos", "a-yin-ya-chu"));
        todasLasPalabras.add(new PalabraDiccionario("Allillanmi", "Estoy bien", "Saludos", "a-yi-yan-mi"));
        todasLasPalabras.add(new PalabraDiccionario("Tupananchiskama", "Hasta luego", "Saludos", "tu-pa-nan-chis-ka-ma"));
        todasLasPalabras.add(new PalabraDiccionario("Sulpayki", "Gracias", "Saludos", "sul-pai-ki"));
        todasLasPalabras.add(new PalabraDiccionario("Añay", "¡Qué lindo!", "Expresiones", "a-ñai"));

        // Familia
        todasLasPalabras.add(new PalabraDiccionario("Tayta", "Padre", "Familia", "tai-ta"));
        todasLasPalabras.add(new PalabraDiccionario("Mama", "Madre", "Familia", "ma-ma"));
        todasLasPalabras.add(new PalabraDiccionario("Wawa", "Bebé/Niño", "Familia", "wa-wa"));
        todasLasPalabras.add(new PalabraDiccionario("Tura", "Hermano", "Familia", "tu-ra"));
        todasLasPalabras.add(new PalabraDiccionario("Ñaña", "Hermana", "Familia", "ña-ña"));

        // Naturaleza
        todasLasPalabras.add(new PalabraDiccionario("Inti", "Sol", "Naturaleza", "in-ti"));
        todasLasPalabras.add(new PalabraDiccionario("Mama Quilla", "Madre Luna", "Naturaleza", "ma-ma ki-ya"));
        todasLasPalabras.add(new PalabraDiccionario("Urpi", "Paloma", "Naturaleza", "ur-pi"));
        todasLasPalabras.add(new PalabraDiccionario("Mayu", "Río", "Naturaleza", "ma-yu"));
        todasLasPalabras.add(new PalabraDiccionario("Qucha", "Lago", "Naturaleza", "ko-cha"));

        // Números
        todasLasPalabras.add(new PalabraDiccionario("Huk", "Uno", "Números", "juk"));
        todasLasPalabras.add(new PalabraDiccionario("Iskay", "Dos", "Números", "is-kai"));
        todasLasPalabras.add(new PalabraDiccionario("Kinsa", "Tres", "Números", "kin-sa"));
        todasLasPalabras.add(new PalabraDiccionario("Tawa", "Cuatro", "Números", "ta-wa"));
        todasLasPalabras.add(new PalabraDiccionario("Pichqa", "Cinco", "Números", "pich-ka"));

        // Verbos
        todasLasPalabras.add(new PalabraDiccionario("Yachay", "Aprender/Saber", "Verbos", "ya-chai"));
        todasLasPalabras.add(new PalabraDiccionario("Munay", "Querer/Amar", "Verbos", "mu-nai"));
        todasLasPalabras.add(new PalabraDiccionario("Puñuy", "Dormir", "Verbos", "pu-ñui"));
        todasLasPalabras.add(new PalabraDiccionario("Mikuy", "Comer", "Verbos", "mi-kui"));
        todasLasPalabras.add(new PalabraDiccionario("Pukllay", "Jugar", "Verbos", "puk-yai"));

        // Frases Típicas
        todasLasPalabras.add(new PalabraDiccionario("Sumaq kawsay", "Buen vivir", "Frases", "su-mak kaw-sai"));
        todasLasPalabras.add(new PalabraDiccionario("Tukuy sunqu", "Con todo el corazón", "Frases", "tu-kui sun-ku"));
        todasLasPalabras.add(new PalabraDiccionario("Ñuqanchik", "Nosotros/as", "Frases", "ñu-kan-chik"));

        palabrasFiltradas = new ArrayList<>(todasLasPalabras);
    }

    private void setupRecyclerView() {
        adapter = new DiccionarioAdapter(palabrasFiltradas, this);
        rvPalabras.setLayoutManager(new LinearLayoutManager(this));
        rvPalabras.setAdapter(adapter);
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
            Chip chip = (Chip) chipGroupCategorias.getChildAt(i);
            chip.setOnClickListener(v -> {
                categoriaSeleccionada = chip.getText().toString();
                filtrarPalabras(etBuscar.getText().toString());
                animarChip(chip);
            });
        }
    }

    private void filtrarPalabras(String query) {
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