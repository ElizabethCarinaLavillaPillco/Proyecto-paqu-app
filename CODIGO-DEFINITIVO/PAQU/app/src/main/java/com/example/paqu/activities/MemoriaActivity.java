package com.example.paqu.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paqu.R;
import android.os.Handler;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoriaActivity extends AppCompatActivity {
    private GridLayout gridLayout;
    private TextView tvScore, tvTimer, tvMoves;
    private CardView cardReset;

    private List<Carta> cartas;
    private Carta primeraCarta = null;
    private Carta segundaCarta = null;
    private boolean bloqueo = false;

    private int puntaje = 0;
    private int movimientos = 0;
    private int paresEncontrados = 0;
    private final int TOTAL_PARES = 6;

    private Handler timerHandler = new Handler();
    private int segundos = 0;
    private boolean juegoActivo = true;

    // Íconos para las cartas (puedes usar emojis o imágenes)
    private final int[] iconos = {
            R.drawable.img_quena,      // Instrumento andino
            R.drawable.img_llama,      // Llama
            R.drawable.img_solinca,       // Sol inca
            R.drawable.img_chakana,    // Cruz andina
            R.drawable.img_montana,    // Montaña
            R.drawable.img_quipu       // Quipu
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memoria);

        inicializarVistas();
        inicializarJuego();
        iniciarTemporizador();
    }

    private void inicializarVistas() {
        gridLayout = findViewById(R.id.gridMemoria);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvMoves = findViewById(R.id.tvMoves);
        cardReset = findViewById(R.id.cardReset);

        cardReset.setOnClickListener(v -> reiniciarJuego());
    }

    private void inicializarJuego() {
        cartas = new ArrayList<>();

        // Duplicar los iconos para crear pares
        List<Integer> iconosDuplicados = new ArrayList<>();
        for (int icono : iconos) {
            iconosDuplicados.add(icono);
            iconosDuplicados.add(icono);
        }

        // Mezclar los iconos
        Collections.shuffle(iconosDuplicados);

        // Crear las cartas
        for (int i = 0; i < iconosDuplicados.size(); i++) {
            Carta carta = new Carta(iconosDuplicados.get(i));
            cartas.add(carta);

            // Crear la vista de la carta
            CardView cardCarta = new CardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 4, 1f);
            params.setMargins(8, 8, 8, 8);
            cardCarta.setLayoutParams(params);
            cardCarta.setCardElevation(8);
            cardCarta.setRadius(16);
            cardCarta.setContentPadding(16, 16, 16, 16);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.drawable.card_back); // Imagen trasera de la carta
            imageView.setTag(i);

            cardCarta.addView(imageView);
            gridLayout.addView(cardCarta);

            final int index = i;
            cardCarta.setOnClickListener(v -> onCartaClick(index));
        }

        actualizarUI();
    }

    private void onCartaClick(int index) {
        if (!juegoActivo || bloqueo || cartas.get(index).isVolteada() || cartas.get(index).isEncontrada()) {
            return;
        }

        voltearCarta(index);

        if (primeraCarta == null) {
            primeraCarta = cartas.get(index);
        } else {
            segundaCarta = cartas.get(index);
            movimientos++;
            actualizarUI();
            verificarPar();
        }
    }

    private void voltearCarta(int index) {
        CardView cardCarta = (CardView) gridLayout.getChildAt(index);
        ImageView imageView = (ImageView) cardCarta.getChildAt(0);

        cartas.get(index).setVolteada(true);
        imageView.setImageResource(cartas.get(index).getIcono());

        // Animación simple
        cardCarta.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction(() -> cardCarta.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }

    private void verificarPar() {
        bloqueo = true;

        new Handler().postDelayed(() -> {
            if (primeraCarta.getIcono() == segundaCarta.getIcono()) {
                // Par encontrado
                primeraCarta.setEncontrada(true);
                segundaCarta.setEncontrada(true);
                paresEncontrados++;
                puntaje += 10;

                if (paresEncontrados == TOTAL_PARES) {
                    juegoTerminado();
                }
            } else {
                // No es par, voltear de nuevo
                primeraCarta.setVolteada(false);
                segundaCarta.setVolteada(false);
                voltearCartaAtras(cartas.indexOf(primeraCarta));
                voltearCartaAtras(cartas.indexOf(segundaCarta));
            }

            primeraCarta = null;
            segundaCarta = null;
            bloqueo = false;
            actualizarUI();
        }, 1000);
    }

    private void voltearCartaAtras(int index) {
        CardView cardCarta = (CardView) gridLayout.getChildAt(index);
        ImageView imageView = (ImageView) cardCarta.getChildAt(0);
        imageView.setImageResource(R.drawable.card_back);
    }

    private void actualizarUI() {
        tvScore.setText("Puntaje: " + puntaje);
        tvMoves.setText("Movimientos: " + movimientos);

        // Formatear tiempo
        int minutos = segundos / 60;
        int segs = segundos % 60;
        tvTimer.setText(String.format("Tiempo: %02d:%02d", minutos, segs));
    }

    private void iniciarTemporizador() {
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (juegoActivo) {
                segundos++;
                actualizarUI();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private void juegoTerminado() {
        juegoActivo = false;
        timerHandler.removeCallbacks(timerRunnable);

        // Bonus por tiempo
        int bonus = Math.max(0, 300 - segundos) / 10;
        puntaje += bonus;
        actualizarUI();

        Toast.makeText(this,
                "¡Felicidades! Puntaje final: " + puntaje +
                        "\nBonus por tiempo: +" + bonus,
                Toast.LENGTH_LONG).show();
    }

    private void reiniciarJuego() {
        timerHandler.removeCallbacks(timerRunnable);
        gridLayout.removeAllViews();

        puntaje = 0;
        movimientos = 0;
        paresEncontrados = 0;
        segundos = 0;
        primeraCarta = null;
        segundaCarta = null;
        juegoActivo = true;

        inicializarJuego();
        iniciarTemporizador();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }


    private int getSelectedNavItemId() {
        return R.id.nav_minijuegos;
    }


    // Clase interna para representar una carta
    private static class Carta {
        private int icono;
        private boolean volteada = false;
        private boolean encontrada = false;

        public Carta(int icono) {
            this.icono = icono;
        }

        public int getIcono() { return icono; }
        public boolean isVolteada() { return volteada; }
        public boolean isEncontrada() { return encontrada; }
        public void setVolteada(boolean volteada) { this.volteada = volteada; }
        public void setEncontrada(boolean encontrada) { this.encontrada = encontrada; }
    }
}