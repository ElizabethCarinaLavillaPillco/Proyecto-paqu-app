package com.example.paqu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.paqu.R;
import com.example.paqu.homeActivity;
import com.example.paqu.managers.FirebaseManager;
import com.google.android.material.button.MaterialButton;

public class LessonResultActivity extends AppCompatActivity {

    private LottieAnimationView lottieResult;
    private TextView tvResultTitle, tvResultSubtitle;
    private TextView tvTimeResult, tvAccuracyResult, tvExpResult, tvStreakResult;
    private TextView tvStreakMessage;
    private MaterialButton btnContinue;

    private boolean isSuccess;
    private FirebaseManager firebaseManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_result);

        initializeViews();
        initializeFirebase();
        loadResults();
    }

    private void initializeViews() {
        lottieResult = findViewById(R.id.lottieResult);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultSubtitle = findViewById(R.id.tvResultSubtitle);
        tvTimeResult = findViewById(R.id.tvTimeResult);
        tvAccuracyResult = findViewById(R.id.tvAccuracyResult);
        tvExpResult = findViewById(R.id.tvExpResult);
        tvStreakResult = findViewById(R.id.tvStreakResult);
        tvStreakMessage = findViewById(R.id.tvStreakMessage);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            // Volver al home
            Intent intent = new Intent(this, homeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initializeFirebase() {
        firebaseManager = FirebaseManager.getInstance();
        userId = firebaseManager.getCurrentUserId();
    }

    private void loadResults() {
        isSuccess = getIntent().getBooleanExtra("success", false);

        if (isSuccess) {
            showSuccessResults();
        } else {
            showFailureResults();
        }
    }

    private void showSuccessResults() {
        // Animación de éxito
        lottieResult.setAnimation("success_celebration.json");
        lottieResult.playAnimation();

        tvResultTitle.setText("¡Felicidades!");
        tvResultTitle.setTextColor(getResources().getColor(R.color.verde));
        tvResultSubtitle.setText("Completaste el reto contrarreloj");

        // Obtener datos del intent
        int timeSpent = getIntent().getIntExtra("timeSpent", 0);
        int accuracy = getIntent().getIntExtra("accuracy", 0);
        int expGained = getIntent().getIntExtra("expGained", 0);

        // Mostrar estadísticas
        tvTimeResult.setText(timeSpent + "s");
        tvAccuracyResult.setText(accuracy + "%");
        tvExpResult.setText("+" + expGained);

        // Obtener y mostrar racha
        firebaseManager.getUserProgress(userId, new FirebaseManager.ProgressCallback() {
            @Override
            public void onSuccess(FirebaseManager.UserProgress progress) {
                int currentStreak = progress.currentStreak != null ? progress.currentStreak : 0;
                tvStreakResult.setText(currentStreak + " días");

                // Verificar si la racha fue actualizada hoy
                checkIfStreakWasUpdated(currentStreak);
            }

            @Override
            public void onError(String error) {
                tvStreakResult.setText("0 días");
            }
        });

        // Cambiar color del botón
        btnContinue.setBackgroundColor(getResources().getColor(R.color.verde));
    }

    // En el metodo showFailureResults(), agregar:
    private void showFailureResults() {
        // Animación de fallo
        lottieResult.setAnimation("sad_llama.json");
        lottieResult.playAnimation();

        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");

        tvResultTitle.setText(title != null ? title : "No completado");
        tvResultTitle.setTextColor(getResources().getColor(R.color.rosado));
        tvResultSubtitle.setText(message != null ? message : "Intenta nuevamente");

        // Ocultar estadísticas detalladas
        findViewById(R.id.cardStats).setVisibility(View.GONE);
        findViewById(R.id.cardRewards).setVisibility(View.GONE);

        // Mostrar botón de reintentar
        MaterialButton btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setVisibility(View.VISIBLE);
        btnRetry.setOnClickListener(v -> {
            // Volver al reto
            finish();
        });

        btnContinue.setText("VOLVER AL INICIO");
        btnContinue.setBackgroundColor(getResources().getColor(R.color.morado));
    }

    private void checkIfStreakWasUpdated(int currentStreak) {
        firebaseManager.updateStreak(userId, new FirebaseManager.StreakCallback() {
            @Override
            public void onStreakUpdated(int streak, boolean wasIncremented) {
                if (wasIncremented) {
                    tvStreakMessage.setVisibility(View.VISIBLE);
                    tvStreakMessage.setText("¡Nueva racha de " + streak + " días! 🎉");
                }
            }

            @Override
            public void onError(String error) {
                // Ignorar error
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Evitar volver atrás, forzar ir al home
        super.onBackPressed();
        Intent intent = new Intent(this, homeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}