package com.example.paqu;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.paqu.models.DiccionarioActivity;
import com.google.android.material.card.MaterialCardView;

public class HerramientasActivity extends BaseActivity {

    // Cards
    MaterialCardView cardTraductor, cardDiccionario, cardPronunciacion;

    // Lottie Animations
    LottieAnimationView lottieTraductor, lottieDiccionario, lottiePronunciacion;

    // Textos
    TextView tvTituloTraductor, tvDescTraductor;
    TextView tvTituloDiccionario, tvDescDiccionario;
    TextView tvTituloPronunciacion, tvDescPronunciacion;

    // Botones
    View btnEmpezarTraductor, btnEmpezarDiccionario, btnEmpezarPronunciacion;

    // Header
    ImageView btnBack;
    TextView tvTituloPantalla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herramientas);

        initViews();
        setupListeners();
        animacionEntrada();
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_dictionary;  // ✅ CORREGIDO: Ahora retorna el ID correcto
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        tvTituloPantalla = findViewById(R.id.tvTituloPantalla);

        // Cards
        cardTraductor = findViewById(R.id.cardTraductor);
        cardDiccionario = findViewById(R.id.cardDiccionario);
        cardPronunciacion = findViewById(R.id.cardPronunciacion);

        // Lottie Animations
        lottieTraductor = findViewById(R.id.lottieTraductor);
        lottieDiccionario = findViewById(R.id.lottieDiccionario);
        lottiePronunciacion = findViewById(R.id.lottiePronunciacion);

        // Textos Traductor
        tvTituloTraductor = findViewById(R.id.tvTituloTraductor);
        tvDescTraductor = findViewById(R.id.tvDescTraductor);

        // Textos Diccionario
        tvTituloDiccionario = findViewById(R.id.tvTituloDiccionario);
        tvDescDiccionario = findViewById(R.id.tvDescDiccionario);

        // Textos Pronunciación
        tvTituloPronunciacion = findViewById(R.id.tvTituloPronunciacion);
        tvDescPronunciacion = findViewById(R.id.tvDescPronunciacion);

        // Botones
        btnEmpezarTraductor = findViewById(R.id.btnEmpezarTraductor);
        btnEmpezarDiccionario = findViewById(R.id.btnEmpezarDiccionario);
        btnEmpezarPronunciacion = findViewById(R.id.btnEmpezarPronunciacion);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            animarClick(v);
            finish();
        });

        // Card Traductor
        cardTraductor.setOnClickListener(v -> {
            animarCardClick(cardTraductor);
            irATraductor();
        });

        btnEmpezarTraductor.setOnClickListener(v -> {
            animarBoton(v);
            irATraductor();
        });

        // Card Diccionario
        cardDiccionario.setOnClickListener(v -> {
            animarCardClick(cardDiccionario);
            irADiccionario();
        });

        btnEmpezarDiccionario.setOnClickListener(v -> {
            animarBoton(v);
            irADiccionario();
        });

        // Card Pronunciación
        cardPronunciacion.setOnClickListener(v -> {
            animarCardClick(cardPronunciacion);
            irAPronunciacion();
        });

        btnEmpezarPronunciacion.setOnClickListener(v -> {
            animarBoton(v);
            irAPronunciacion();
        });
    }

    private void irATraductor() {
        Intent intent = new Intent(this, TraductorActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void irADiccionario() {
        Intent intent = new Intent(this, DiccionarioActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void irAPronunciacion() {
        Intent intent = new Intent(this, PronunciacionActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    // ============= ANIMACIONES =============

    private void animacionEntrada() {
        View[] cards = {cardTraductor, cardDiccionario, cardPronunciacion};

        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            card.setAlpha(0f);
            card.setTranslationX(300f);

            card.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(600)
                    .setStartDelay(i * 150)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }

        // Animar Lotties
        lottieTraductor.playAnimation();
        lottieDiccionario.playAnimation();
        lottiePronunciacion.playAnimation();
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                ).start();
    }

    private void animarCardClick(View card) {
        // Efecto de pulsación
        card.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        card.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                ).start();

        // Efecto de elevación
        ObjectAnimator elevationAnim = ObjectAnimator.ofFloat(card, "elevation", 8f, 16f, 8f);
        elevationAnim.setDuration(300);
        elevationAnim.start();
    }

    private void animarBoton(View btn) {
        btn.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(100)
                .withEndAction(() ->
                        btn.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                ).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reiniciar animaciones Lottie
        lottieTraductor.playAnimation();
        lottieDiccionario.playAnimation();
        lottiePronunciacion.playAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar animaciones Lottie para ahorrar batería
        lottieTraductor.pauseAnimation();
        lottieDiccionario.pauseAnimation();
        lottiePronunciacion.pauseAnimation();
    }
}