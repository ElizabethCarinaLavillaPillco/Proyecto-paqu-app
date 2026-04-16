package com.example.paqu.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paqu.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
public class InfoSoporteActivity extends AppCompatActivity {

    private Button btnContactarEmail, btnReportarProblema;
    private MaterialCardView cardFAQ1, cardFAQ2;
    private TextView tvRespuesta1, tvRespuesta2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_soporte);

        initViews();
        setupClickListeners();
        setupFAQCards();
    }

    private void initViews() {
        btnContactarEmail = findViewById(R.id.btnContactarEmail);
        btnReportarProblema = findViewById(R.id.btnReportarProblema);
        cardFAQ1 = findViewById(R.id.cardFAQ1);
        cardFAQ2 = findViewById(R.id.cardFAQ2);
        tvRespuesta1 = findViewById(R.id.tvRespuesta1);
        tvRespuesta2 = findViewById(R.id.tvRespuesta2);
    }

    private void setupClickListeners() {
        btnContactarEmail.setOnClickListener(v -> contactarPorEmail());
        btnReportarProblema.setOnClickListener(v -> reportarProblema());
    }

    private void setupFAQCards() {
        cardFAQ1.setOnClickListener(v -> toggleFAQAnswer(tvRespuesta1));
        cardFAQ2.setOnClickListener(v -> toggleFAQAnswer(tvRespuesta2));
    }

    private void toggleFAQAnswer(TextView textView) {
        if (textView.getVisibility() == View.VISIBLE) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void contactarPorEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:soporte@paquapp.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Soporte PAQU App");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola equipo PAQU,\n\nNecesito ayuda con:");

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar email..."));
        } catch (Exception e) {
            // Manejar error
        }
    }

    private void reportarProblema() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:bugs@paquapp.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Problema - PAQU App");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Descripción del problema:\n\nPasos para reproducir:\n1.\n2.\n3.\n\nDispositivo: ");

        try {
            startActivity(Intent.createChooser(emailIntent, "Reportar problema..."));
        } catch (Exception e) {
            // Manejar error
        }
    }
}