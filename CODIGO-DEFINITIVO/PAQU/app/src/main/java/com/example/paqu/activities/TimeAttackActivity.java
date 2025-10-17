package com.example.paqu.activities;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.paqu.R;
import com.example.paqu.generators.ExerciseGenerator;
import com.example.paqu.managers.FirebaseManager;
import com.example.paqu.models.TimeAttackExercise;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class TimeAttackActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTimer, tvHearts, tvDiamonds;
    private ProgressBar progressBar;
    private FrameLayout exerciseContainer, feedbackOverlay;
    private LottieAnimationView lottieAnimation;

    // Timer
    private CountDownTimer countDownTimer;
    private long timeRemaining = 60000; // 60 segundos
    private static final long TIMER_DURATION = 60000;

    // Game State
    private List<TimeAttackExercise> exercises;
    private int currentExerciseIndex = 0;
    private int hearts = 5;
    private int diamonds = 0;
    private int correctAnswers = 0;
    private int totalExercises = 10;
    private long startTime;
    private ArrayList<String> mistakes = new ArrayList<>();

    // Audio
    private MediaPlayer mediaPlayer;

    // Firebase
    private FirebaseManager firebaseManager;
    private String userId;

    // Animation
    private boolean isShowingFeedback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_attack);

        initializeViews();
        initializeFirebase();
        showStartAnimation();
    }

    private void initializeViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvHearts = findViewById(R.id.tvHearts);
        tvDiamonds = findViewById(R.id.tvDiamonds);
        progressBar = findViewById(R.id.progressBar);
        exerciseContainer = findViewById(R.id.exerciseContainer);
        feedbackOverlay = findViewById(R.id.feedbackOverlay);
        lottieAnimation = findViewById(R.id.lottieAnimation);

        progressBar.setMax(totalExercises);
        progressBar.setProgress(0);
    }

    private void initializeFirebase() {
        firebaseManager = FirebaseManager.getInstance();
        userId = firebaseManager.getCurrentUserId();

        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProgress();
    }

    private void loadUserProgress() {
        firebaseManager.getUserProgress(userId, new FirebaseManager.ProgressCallback() {
            @Override
            public void onSuccess(FirebaseManager.UserProgress progress) {
                hearts = progress.hearts;
                diamonds = progress.coins;

                tvHearts.setText(String.valueOf(hearts));
                tvDiamonds.setText(String.valueOf(diamonds));

                // Verificar vidas antes de empezar
                if (hearts <= 0) {
                    firebaseManager.checkAndRefillHearts(userId, new FirebaseManager.HeartsCallback() {
                        @Override
                        public void onHeartsChecked(int heartsCount, Long nextRefresh) {
                            hearts = heartsCount;
                            tvHearts.setText(String.valueOf(hearts));

                            if (hearts <= 0) {
                                showInsufficientHeartsDialog();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(TimeAttackActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TimeAttackActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStartAnimation() {
        // Crear diálogo de inicio
        Dialog startDialog = new Dialog(this);
        startDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        startDialog.setContentView(R.layout.dialog_start_countdown);
        startDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        startDialog.setCancelable(false);

        LottieAnimationView lottieStart = startDialog.findViewById(R.id.lottieStartAnimation);
        TextView tvCountdown = startDialog.findViewById(R.id.tvCountdown);

        lottieStart.setAnimation("ready_go.json");
        lottieStart.playAnimation();

        startDialog.show();

        // Countdown: 3, 2, 1, ¡COMIENZA!
        new Handler().postDelayed(() -> {
            tvCountdown.setText("3");
            tvCountdown.setTextSize(72);
            animateText(tvCountdown);
        }, 500);

        new Handler().postDelayed(() -> {
            tvCountdown.setText("2");
            animateText(tvCountdown);
        }, 1500);

        new Handler().postDelayed(() -> {
            tvCountdown.setText("1");
            animateText(tvCountdown);
        }, 2500);

        new Handler().postDelayed(() -> {
            tvCountdown.setText("¡COMIENZA!");
            tvCountdown.setTextSize(48);
            animateText(tvCountdown);
        }, 3500);

        new Handler().postDelayed(() -> {
            startDialog.dismiss();
            startLesson();
        }, 4500);
    }

    private void animateText(TextView textView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 0.5f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 0.5f, 1.2f, 1.0f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();
    }

    private void startLesson() {
        // Generar ejercicios
        exercises = ExerciseGenerator.generateRandomExercises(totalExercises);
        startTime = System.currentTimeMillis();

        // Iniciar timer
        startTimer();

        // Cargar primer ejercicio
        loadExercise(currentExerciseIndex);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.valueOf(secondsRemaining));

                // Cambiar color cuando quedan menos de 10 segundos
                if (secondsRemaining <= 10) {
                    tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                    // Animación de pulso
                    if (secondsRemaining <= 10 && secondsRemaining > 0) {
                        Animation pulse = AnimationUtils.loadAnimation(TimeAttackActivity.this, R.anim.pulse_animation);
                        tvTimer.startAnimation(pulse);
                    }
                } else {
                    tvTimer.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0");
                endLessonTimedOut();
            }
        }.start();
    }

    private void loadExercise(int index) {
        if (index >= exercises.size()) {
            endLessonSuccess();
            return;
        }

        TimeAttackExercise exercise = exercises.get(index);
        exerciseContainer.removeAllViews();

        switch (exercise.getType()) {
            case SELECT_CORRECT:
                loadSelectWordExercise(exercise);
                break;
            case FORM_SENTENCE:
                loadFormSentenceExercise(exercise);
                break;
            case TRANSLATE:
                loadTranslateExercise(exercise);
                break;
            default:
                loadSelectWordExercise(exercise);
        }

        // Actualizar progreso
        progressBar.setProgress(index + 1);
    }

    private void loadSelectWordExercise(TimeAttackExercise exercise) {
        View exerciseView = LayoutInflater.from(this)
                .inflate(R.layout.exercise_select_word, exerciseContainer, false);

        ImageView ivImage = exerciseView.findViewById(R.id.ivExerciseImage);
        TextView tvQuestion = exerciseView.findViewById(R.id.tvQuestion);
        MaterialButton btnPlayAudio = exerciseView.findViewById(R.id.btnPlayAudio);
        FlexboxLayout flexboxOptions = exerciseView.findViewById(R.id.flexboxOptions);

        // Configurar imagen
        if (exercise.getImageResId() != 0) {
            ivImage.setImageResource(exercise.getImageResId());
        }

        // Configurar pregunta
        tvQuestion.setText(exercise.getQuestion());

        // Configurar botón de audio
        btnPlayAudio.setOnClickListener(v -> playAudio(exercise.getAudioResId()));

        // Crear opciones
        flexboxOptions.removeAllViews();
        for (String option : exercise.getOptions()) {
            MaterialButton btnOption = createOptionButton(option);
            btnOption.setOnClickListener(v -> checkAnswer(option, exercise.getCorrectAnswer()));
            flexboxOptions.addView(btnOption);
        }

        exerciseContainer.addView(exerciseView);

        // Auto-reproducir audio
        new Handler().postDelayed(() -> playAudio(exercise.getAudioResId()), 300);
    }

    private void loadFormSentenceExercise(TimeAttackExercise exercise) {
        View exerciseView = LayoutInflater.from(this)
                .inflate(R.layout.exercise_form_sentence, exerciseContainer, false);

        TextView tvQuestion = exerciseView.findViewById(R.id.tvQuestionSentence);
        MaterialButton btnPlayAudio = exerciseView.findViewById(R.id.btnPlayAudioSentence);
        FlexboxLayout flexboxAnswer = exerciseView.findViewById(R.id.flexboxAnswer);
        FlexboxLayout flexboxWords = exerciseView.findViewById(R.id.flexboxWords);
        MaterialButton btnCheck = exerciseView.findViewById(R.id.btnCheckSentence);

        tvQuestion.setText(exercise.getQuestion());
        btnPlayAudio.setOnClickListener(v -> playAudio(exercise.getAudioResId()));

        List<String> userAnswer = new ArrayList<>();

        // Crear palabras disponibles
        for (String word : exercise.getOptions()) {
            MaterialButton btnWord = createWordChip(word);
            btnWord.setOnClickListener(v -> {
                userAnswer.add(word);
                flexboxAnswer.addView(createWordChip(word));
                flexboxWords.removeView(btnWord);
                btnCheck.setEnabled(true);
            });
            flexboxWords.addView(btnWord);
        }

        // Permitir remover palabras del área de respuesta
        flexboxAnswer.setOnClickListener(v -> {
            if (flexboxAnswer.getChildCount() > 0) {
                View lastWord = flexboxAnswer.getChildAt(flexboxAnswer.getChildCount() - 1);
                String wordText = ((MaterialButton) lastWord).getText().toString();
                userAnswer.remove(userAnswer.size() - 1);
                flexboxAnswer.removeView(lastWord);

                MaterialButton btnWord = createWordChip(wordText);
                btnWord.setOnClickListener(vv -> {
                    userAnswer.add(wordText);
                    flexboxAnswer.addView(createWordChip(wordText));
                    flexboxWords.removeView(btnWord);
                    btnCheck.setEnabled(true);
                });
                flexboxWords.addView(btnWord);
            }
        });

        btnCheck.setOnClickListener(v -> {
            String answer = String.join(" ", userAnswer);
            checkAnswer(answer, exercise.getCorrectAnswer());
        });

        exerciseContainer.addView(exerciseView);
        playAudio(exercise.getAudioResId());
    }

    private void loadTranslateExercise(TimeAttackExercise exercise) {
        // Similar a SELECT_CORRECT
        loadSelectWordExercise(exercise);
    }

    private MaterialButton createOptionButton(String text) {
        MaterialButton button = new MaterialButton(this);
        button.setText(text);
        button.setTextSize(16);
        button.setBackgroundColor(getResources().getColor(R.color.white));
        button.setTextColor(getResources().getColor(R.color.black));
        button.setCornerRadius(60);
        button.setStrokeWidth(4);
        button.setStrokeColor(getResources().getColorStateList(R.color.moradit));

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 12, 12, 12);
        button.setLayoutParams(params);
        button.setPadding(40, 24, 40, 24);

        return button;
    }

    private MaterialButton createWordChip(String text) {
        MaterialButton chip = new MaterialButton(this);
        chip.setText(text);
        chip.setTextSize(16);
        chip.setBackgroundColor(getResources().getColor(R.color.rosado));
        chip.setTextColor(getResources().getColor(R.color.black));
        chip.setCornerRadius(50);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        chip.setLayoutParams(params);
        chip.setPadding(32, 20, 32, 20);

        return chip;
    }

    private void playAudio(int audioResId) {
        if (audioResId == 0) return;

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, audioResId);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// Continúa en la siguiente parte...
// Continuación de TimeAttackActivity.java

    private void checkAnswer(String userAnswer, String correctAnswer) {
        if (isShowingFeedback) return;
        isShowingFeedback = true;

        boolean isCorrect = userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());

        if (isCorrect) {
            correctAnswers++;
            showFeedbackAnimation(true);
        } else {
            mistakes.add(exercises.get(currentExerciseIndex).getId());
            loseHeart();
            showFeedbackAnimation(false);
        }
    }

    private void showFeedbackAnimation(boolean isCorrect) {
        feedbackOverlay.setVisibility(View.VISIBLE);

        if (isCorrect) {
            //sonido de exito
            lottieAnimation.setAnimation("success.json");
            playSound(R.raw.voz2);
        } else {
            //sonid de vo< eorro
            lottieAnimation.setAnimation("cross_error.json");
            playSound(R.raw.voz1);
        }

        lottieAnimation.playAnimation();

        new Handler().postDelayed(() -> {
            feedbackOverlay.setVisibility(View.GONE);
            isShowingFeedback = false;

            // Siguiente ejercicio
            currentExerciseIndex++;
            loadExercise(currentExerciseIndex);
        }, 1500);
    }

    private void loseHeart() {
        hearts--;
        tvHearts.setText(String.valueOf(hearts));

        // Animación de corazón perdido
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
        tvHearts.startAnimation(shake);

        // Actualizar en Firebase
        firebaseManager.loseHeart(userId, new FirebaseManager.HeartLostCallback() {
            @Override
            public void onHeartLost(int remainingHearts) {
                if (remainingHearts <= 0) {
                    pauseTimer();
                    showNoHeartsDialog();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TimeAttackActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoHeartsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_no_hearts);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        LottieAnimationView lottieCrying = dialog.findViewById(R.id.lottieCrying);
        TextView tvAvailableDiamonds = dialog.findViewById(R.id.tvAvailableDiamonds);
        MaterialButton btnUseDiamonds = dialog.findViewById(R.id.btnUseDiamonds);
        MaterialButton btnEndLesson = dialog.findViewById(R.id.btnEndLesson);

        lottieCrying.setAnimation("crying_llama.json");
        lottieCrying.playAnimation();

        tvAvailableDiamonds.setText("Tienes: " + diamonds + " 💎");

        // Verificar si tiene suficientes diamantes
        if (diamonds < 100) {
            btnUseDiamonds.setEnabled(false);
            btnUseDiamonds.setAlpha(0.5f);
        } else {
            btnUseDiamonds.setOnClickListener(v -> {
                useDiamondsForHearts(dialog);
            });
        }

        btnEndLesson.setOnClickListener(v -> {
            dialog.dismiss();
            endLessonFailed();
        });

        dialog.show();
    }

    private void useDiamondsForHearts(Dialog dialog) {
        firebaseManager.useCoinsForHearts(userId, new FirebaseManager.CoinsCallback() {
            @Override
            public void onCoinsUsed(int remainingCoins) {
                diamonds = remainingCoins;
                hearts = 5;

                tvDiamonds.setText(String.valueOf(diamonds));
                tvHearts.setText(String.valueOf(hearts));

                Toast.makeText(TimeAttackActivity.this,
                        "¡Vidas restauradas! 💎", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                resumeTimer();
            }

            @Override
            public void onInsufficientCoins(int currentCoins) {
                Toast.makeText(TimeAttackActivity.this,
                        "No tienes suficientes diamantes", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TimeAttackActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void resumeTimer() {
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.valueOf(secondsRemaining));

                if (secondsRemaining <= 10) {
                    tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Animation pulse = AnimationUtils.loadAnimation(TimeAttackActivity.this, R.anim.pulse_animation);
                    tvTimer.startAnimation(pulse);
                }
            }

            @Override
            public void onFinish() {
                endLessonTimedOut();
            }
        }.start();
    }

    private void endLessonSuccess() {
        pauseTimer();

        long endTime = System.currentTimeMillis();
        int timeSpent = (int) ((endTime - startTime) / 1000);
        double accuracy = (double) correctAnswers / totalExercises;
        int expGained = calculateExp(accuracy, timeSpent);
        int coinsGained = calculateCoins(accuracy);

        // Guardar en Firebase
        FirebaseManager.LessonResult result = new FirebaseManager.LessonResult();
        result.completed = true;
        result.score = accuracy;
        result.timeSpent = timeSpent;
        result.attempts = 1;
        result.perfect = (correctAnswers == totalExercises);
        result.expGained = expGained;
        result.coinsGained = coinsGained;
        result.mistakes = mistakes.toArray(new String[0]);

        firebaseManager.completeLesson(userId, "time_attack_saludos", result,
                new FirebaseManager.CompletionCallback() {
                    @Override
                    public void onLessonCompleted(FirebaseManager.LessonResult result) {
                        updateStreak();
                        showSuccessScreen(timeSpent, accuracy, expGained);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(TimeAttackActivity.this, error, Toast.LENGTH_SHORT).show();
                        showSuccessScreen(timeSpent, accuracy, expGained);
                    }
                });
    }

    private void endLessonTimedOut() {
        pauseTimer();
        showFailureScreen("¡Se acabó el tiempo!", "No lograste terminar a tiempo");
    }

    private void endLessonFailed() {
        pauseTimer();
        showFailureScreen("¡Vuelve a intentarlo!", "Te quedaste sin vidas");
    }

    private void updateStreak() {
        firebaseManager.updateStreak(userId, new FirebaseManager.StreakCallback() {
            @Override
            public void onStreakUpdated(int currentStreak, boolean wasIncremented) {
                // La racha se mostrará en la pantalla de resultados
            }

            @Override
            public void onError(String error) {
                // Ignorar error de racha
            }
        });
    }

    private void showSuccessScreen(int timeSpent, double accuracy, int expGained) {
        // Primero mostrar animación de éxito con confeti
        showCelebrationAnimation();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, LessonResultActivity.class);
            intent.putExtra("success", true);
            intent.putExtra("timeSpent", timeSpent);
            intent.putExtra("accuracy", (int) (accuracy * 100));
            intent.putExtra("expGained", expGained);
            intent.putExtra("correctAnswers", correctAnswers);
            intent.putExtra("totalExercises", totalExercises);
            startActivity(intent);
            finish();
        }, 4000);
    }

    private void showFailureScreen(String title, String message) {
        Intent intent = new Intent(this, LessonResultActivity.class);
        intent.putExtra("success", false);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        startActivity(intent);
        finish();
    }

    private void showCelebrationAnimation() {
        Dialog celebrationDialog = new Dialog(this);
        celebrationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        celebrationDialog.setContentView(R.layout.dialog_celebration);
        celebrationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        celebrationDialog.setCancelable(false);

        LottieAnimationView lottieConfetti = celebrationDialog.findViewById(R.id.lottieConfetti);
        LottieAnimationView lottieLlama = celebrationDialog.findViewById(R.id.lottieLlama);
        TextView tvCelebration = celebrationDialog.findViewById(R.id.tvCelebration);

        lottieConfetti.setAnimation("confetti.json");
        lottieConfetti.playAnimation();

        lottieLlama.setAnimation("llama_happy.json");
        lottieLlama.playAnimation();

        tvCelebration.setText("¡Lo lograste!");

        celebrationDialog.show();

        new Handler().postDelayed(celebrationDialog::dismiss, 4000);
    }

    private void showInsufficientHeartsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sin vidas")
                .setMessage("No tienes vidas suficientes para jugar. Espera o usa diamantes.")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private int calculateExp(double accuracy, int timeSpent) {
        int baseExp = 50;
        int accuracyBonus = (int) (accuracy * 30);
        int speedBonus = (timeSpent < 45) ? 20 : 0;
        return baseExp + accuracyBonus + speedBonus;
    }

    private int calculateCoins(double accuracy) {
        int baseCoins = 10;
        int accuracyBonus = (int) (accuracy * 15);
        return baseCoins + accuracyBonus;
    }

    private void playSound(int soundResId) {
        try {
            MediaPlayer sound = MediaPlayer.create(this, soundResId);
            sound.start();
            sound.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("¿Salir?")
                .setMessage("Perderás tu progreso actual")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Continuar", null)
                .show();
    }
}