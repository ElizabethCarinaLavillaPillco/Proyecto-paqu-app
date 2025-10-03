package com.example.avatar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class FelicidadesActivity extends AppCompatActivity {

    private TextView tvMensaje;
    private ImageView imgAvatar;
    private LottieAnimationView confettiAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_felicidades);

        tvMensaje = findViewById(R.id.tvMensaje);
        imgAvatar = findViewById(R.id.imgAvatarSeleccionado);
        confettiAnim = findViewById(R.id.confettiAnim);

        // Recuperar datos del Intent
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        int imagenResId = intent.getIntExtra("imagen", -1);

        // Mostrar en la UI
        tvMensaje.setText("🎉 ¡Felicidades por escoger " +"  " +nombre +""+ "! 🎉");
        if (imagenResId != -1) {
            imgAvatar.setImageResource(imagenResId);
        }

        // Iniciar animación
        confettiAnim.playAnimation();
    }
}
