package com.example.paqu.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.paqu.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoriaActivity extends AppCompatActivity {
    private GridLayout gridLayout;
    private TextView tvScore, tvTimer, tvMoves;
    private CardView cardReset;

    private List<Integer> cartasVolteadas = new ArrayList<>();
    private List<Integer> cartasEncontradas = new ArrayList<>();
    private boolean bloqueo = false;

    private int puntaje = 0;
    private int movimientos = 0;
    private int segundos = 0;
    private boolean juegoActivo = true;

    private Handler timerHandler = new Handler();

    // Íconos para las cartas
    private final int[] iconos = {
            R.drawable.img_quena,
            R.drawable.img_llama,
            R.drawable.img_solinca,
            R.drawable.img_chakana,
            R.drawable.img_montana,
            R.drawable.img_quipu
    };

    // Nombres de las imágenes
    private final String[] nombres = {
            "Quena", "Llama", "Sol Inca", "Chakana", "Montaña", "Quipu"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memoria);

        encontrarVistas();
        configurarJuego();
        iniciarTemporizador();
    }

    private void encontrarVistas() {
        gridLayout = findViewById(R.id.gridMemoria);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvMoves = findViewById(R.id.tvMoves);
        cardReset = findViewById(R.id.cardReset);

        cardReset.setOnClickListener(v -> reiniciarJuego());
    }

    private void configurarJuego() {
        gridLayout.removeAllViews();

        List<Integer> cartas = new ArrayList<>();

        // Crear pares de cartas
        for (int i = 0; i < iconos.length; i++) {
            cartas.add(i); // Índice de la imagen
            cartas.add(i); // Par
        }

        // Mezclar cartas
        Collections.shuffle(cartas);

        // Crear grid 4x3
        int totalCartas = 12;
        for (int i = 0; i < totalCartas; i++) {
            CardView carta = crearCarta(cartas.get(i), i);
            gridLayout.addView(carta);
        }

        actualizarUI();
    }

    private CardView crearCarta(final int imagenIndex, final int posicion) {
        // Crear CardView
        CardView cardView = new CardView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = dpToPx(80);
        params.height = dpToPx(100);
        params.setMargins(4, 4, 4, 4);
        cardView.setLayoutParams(params);

        cardView.setCardElevation(8);
        cardView.setRadius(12);
        cardView.setContentPadding(8, 8, 8, 8);

        // Crear ImageView
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.card_back);
        imageView.setTag(imagenIndex); // Guardar índice de la imagen

        cardView.addView(imageView);

        // Click listener
        cardView.setOnClickListener(v -> {
            if (juegoActivo && !bloqueo) {
                manejarClickCarta(posicion, imagenIndex, cardView, imageView);
            }
        });

        return cardView;
    }

    private void manejarClickCarta(int posicion, int imagenIndex, CardView cardView, ImageView imageView) {
        // Si la carta ya está volteada o encontrada, ignorar
        if (cartasVolteadas.contains(posicion) || cartasEncontradas.contains(imagenIndex)) {
            return;
        }

        // Voltear carta
        cartasVolteadas.add(posicion);
        imageView.setImageResource(iconos[imagenIndex]);

        // Mostrar nombre en Toast
        Toast.makeText(this, nombres[imagenIndex], Toast.LENGTH_SHORT).show();

        // Verificar si hay dos cartas volteadas
        if (cartasVolteadas.size() == 2) {
            movimientos++;
            bloqueo = true;

            new Handler().postDelayed(() -> {
                verificarPar();
                bloqueo = false;
            }, 1000);
        }

        actualizarUI();
    }

    private void verificarPar() {
        if (cartasVolteadas.size() != 2) return;

        // Obtener las dos cartas volteadas
        int pos1 = cartasVolteadas.get(0);
        int pos2 = cartasVolteadas.get(1);

        CardView carta1 = (CardView) gridLayout.getChildAt(pos1);
        CardView carta2 = (CardView) gridLayout.getChildAt(pos2);

        int imgIndex1 = (int) ((ImageView) carta1.getChildAt(0)).getTag();
        int imgIndex2 = (int) ((ImageView) carta2.getChildAt(0)).getTag();

        if (imgIndex1 == imgIndex2) {
            // Par encontrado
            cartasEncontradas.add(imgIndex1);
            puntaje += 10;

            if (cartasEncontradas.size() == iconos.length) {
                juegoTerminado();
            }
        } else {
            // Voltear cartas de nuevo
            voltearCartaAtras(carta1);
            voltearCartaAtras(carta2);
        }

        cartasVolteadas.clear();
        actualizarUI();
    }

    private void voltearCartaAtras(CardView carta) {
        ImageView imageView = (ImageView) carta.getChildAt(0);
        imageView.setImageResource(R.drawable.card_back);
    }

    private void actualizarUI() {
        tvScore.setText("Puntaje: " + puntaje);
        tvMoves.setText("Movimientos: " + movimientos);

        int minutos = segundos / 60;
        int segs = segundos % 60;
        tvTimer.setText(String.format("Tiempo: %02d:%02d", minutos, segs));
    }

    private void iniciarTemporizador() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (juegoActivo) {
                    segundos++;
                    actualizarUI();
                    timerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void juegoTerminado() {
        juegoActivo = false;
        timerHandler.removeCallbacksAndMessages(null);

        Toast.makeText(this,
                "¡Ganaste! Puntaje: " + puntaje +
                        "\nTiempo: " + segundos + " segundos",
                Toast.LENGTH_LONG).show();
    }

    private void reiniciarJuego() {
        timerHandler.removeCallbacksAndMessages(null);

        puntaje = 0;
        movimientos = 0;
        segundos = 0;
        juegoActivo = true;

        cartasVolteadas.clear();
        cartasEncontradas.clear();

        configurarJuego();
        iniciarTemporizador();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }
}