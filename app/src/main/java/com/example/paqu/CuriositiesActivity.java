package com.example.paqu;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class CuriositiesActivity extends AppCompatActivity {

    private ImageView curiosityImage;
    private TextView curiosityTitle;
    private TextView curiosityDescription;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private TextView pageIndicator;

    private int currentIndex = 0;

    // Arrays con tus recursos
    private int[] curiosityImages = {
            R.drawable.curiosidad1,
            R.drawable.curiosidad2,
            R.drawable.curiosidad3,
            R.drawable.curiosidad4
    };

    private String[] curiosityTitles = {
            "Origen del Quechua",
            "Hablantes del Quechua",
            "Variantes Dialectales",
            "El Quechua en la Actualidad",
            "Patrimonio Cultural"
            // Agrega más títulos correspondientes a tus imágenes
    };

    private String[] curiosityDescriptions = {
            "El quechua es una familia de lenguas originarias de los Andes centrales que se extiende por siete países de Sudamérica.",
            "Aproximadamente 8-10 millones de personas hablan quechua en países como Perú, Bolivia, Ecuador, Colombia, Chile, Argentina y Brasil.",
            "Existen numerosas variantes del quechua, siendo el quechua cusqueño y el quechua ayacuchano los más conocidos en Perú.",
            "A pesar de ser una lengua ancestral, el quechua se mantiene vivo gracias a esfuerzos de revitalización cultural y educación bilingüe.",
            "En 1975, el quechua fue declarado lengua oficial del Perú junto al español, reconociendo su importancia cultural e histórica."
            // Agrega más descripciones
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curiosities);

        initializeViews();
        setupListeners();
        updateCuriosity();
    }

    private void initializeViews() {
        curiosityImage = findViewById(R.id.curiosityImage);
        curiosityTitle = findViewById(R.id.curiosityTitle);
        curiosityDescription = findViewById(R.id.curiosityDescription);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        pageIndicator = findViewById(R.id.pageIndicator);

        CardView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateCuriosity();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < curiosityImages.length - 1) {
                currentIndex++;
                updateCuriosity();
            }
        });
    }

    private void updateCuriosity() {
        // Actualizar imagen
        curiosityImage.setImageResource(curiosityImages[currentIndex]);

        // Actualizar título
        curiosityTitle.setText(curiosityTitles[currentIndex]);

        // Actualizar descripción
        curiosityDescription.setText(curiosityDescriptions[currentIndex]);

        // Actualizar indicador de página
        pageIndicator.setText((currentIndex + 1) + " / " + curiosityImages.length);

        // Actualizar visibilidad de botones
        btnPrevious.setEnabled(currentIndex > 0);
        btnPrevious.setAlpha(currentIndex > 0 ? 1.0f : 0.3f);

        btnNext.setEnabled(currentIndex < curiosityImages.length - 1);
        btnNext.setAlpha(currentIndex < curiosityImages.length - 1 ? 1.0f : 0.3f);
    }
}