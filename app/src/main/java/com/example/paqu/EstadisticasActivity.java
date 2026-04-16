package com.example.paqu;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.paqu.managers.StatisticsManager;
import com.example.paqu.models.WeeklyStats;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

/**
 * Activity para mostrar estadísticas semanales del usuario
 */
public class EstadisticasActivity extends BaseActivity {

    private static final String TAG = "EstadisticasActivity";

    // Views del resumen
    private TextView tvTotalLessons, tvTotalTime, tvTotalExp;

    // Views de lecciones
    private LinearLayout lessonsBreakdown;
    private TextView tvMostActiveDay;

    // Views de tiempo
    private TextView tvAverageDailyTime, tvWeeklyTotalTime;

    // Views del gráfico
    private LinearLayout chartContainer, daysLegend;

    // Mensaje motivacional
    private TextView tvMotivationalMessage;

    // Manager
    private StatisticsManager statisticsManager;
    private String userId;

    // Audio Manager
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        initializeViews();
        initializeManagers();
        loadStatistics();
        setupBackButton();
    }

    private void initializeViews() {
        // Resumen
        tvTotalLessons = findViewById(R.id.tvTotalLessons);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalExp = findViewById(R.id.tvTotalExp);

        // Lecciones
        lessonsBreakdown = findViewById(R.id.lessonsBreakdown);
        tvMostActiveDay = findViewById(R.id.tvMostActiveDay);

        // Tiempo
        tvAverageDailyTime = findViewById(R.id.tvAverageDailyTime);
        tvWeeklyTotalTime = findViewById(R.id.tvWeeklyTotalTime);

        // Gráfico
        chartContainer = findViewById(R.id.chartContainer);
        daysLegend = findViewById(R.id.daysLegend);

        // Mensaje motivacional
        tvMotivationalMessage = findViewById(R.id.tvMotivationalMessage);
    }

    private void initializeManagers() {
        statisticsManager = new StatisticsManager();
        audioManager = AudioManager.getInstance(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadStatistics() {
        if (userId == null) return;

        Log.d(TAG, "📊 Cargando estadísticas...");

        statisticsManager.getWeeklyStats(userId, new StatisticsManager.StatsCallback() {
            @Override
            public void onSuccess(WeeklyStats stats) {
                Log.d(TAG, "✅ Estadísticas cargadas exitosamente");
                runOnUiThread(() -> {
                    displayStatistics(stats);
                    animateViews();
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error al cargar estadísticas: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(EstadisticasActivity.this,
                            "Error al cargar estadísticas", Toast.LENGTH_SHORT).show();
                    displayEmptyStatistics();
                });
            }
        });
    }

    private void displayStatistics(WeeklyStats stats) {
        // RESUMEN
        animateNumber(tvTotalLessons, stats.getTotalLessonsCompleted());
        animateNumber(tvTotalTime, (int) stats.getTotalStudyTimeMinutes());
        animateNumber(tvTotalExp, stats.getTotalExperienceGained());

        // GRÁFICO DE BARRAS
        displayWeeklyChart(stats);

        // LECCIONES COMPLETADAS
        displayLessonsBreakdown(stats);
        tvMostActiveDay.setText(stats.getMostActiveDay());

        // TIEMPO DE ESTUDIO
        double avgTime = stats.getAverageDailyStudyTime();
        tvAverageDailyTime.setText(String.format(Locale.getDefault(), "%.0f min", avgTime));
        tvWeeklyTotalTime.setText(String.format(Locale.getDefault(), "%d min",
                stats.getTotalStudyTimeMinutes()));

        // MENSAJE MOTIVACIONAL
        displayMotivationalMessage(stats);
    }

    /**
     * Muestra el gráfico de barras de la semana
     */
    private void displayWeeklyChart(WeeklyStats stats) {
        chartContainer.removeAllViews();
        daysLegend.removeAllViews();

        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        int[] colors = {
                R.color.morado, R.color.rosado, R.color.verde,
                R.color.amarillo, R.color.naranja, R.color.celeste, R.color.lila
        };

        // Encontrar el máximo para escalar las barras
        int maxLessons = 1;
        for (String day : days) {
            WeeklyStats.DayStats dayStats = stats.getWeekData().get(day);
            if (dayStats != null && dayStats.getLessonsCompleted() > maxLessons) {
                maxLessons = dayStats.getLessonsCompleted();
            }
        }

        // Crear barras
        for (int i = 0; i < days.length; i++) {
            String day = days[i];
            WeeklyStats.DayStats dayStats = stats.getWeekData().get(day);
            int lessons = dayStats != null ? dayStats.getLessonsCompleted() : 0;

            // Contenedor de la barra
            LinearLayout barContainer = new LinearLayout(this);
            barContainer.setOrientation(LinearLayout.VERTICAL);
            barContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER);
            barContainer.setPadding(12, 0, 12, 0);

            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            barContainer.setLayoutParams(containerParams);

            // Vista de la barra
            View bar = new View(this);
            int barColor = ContextCompat.getColor(this, colors[i]);
            bar.setBackgroundColor(barColor);

            // Calcular altura de la barra (máximo 150dp)
            int maxHeight = (int) (150 * getResources().getDisplayMetrics().density);
            int barHeight = maxLessons > 0 ? (lessons * maxHeight) / maxLessons : 0;
            barHeight = Math.max(barHeight, 8); // Altura mínima

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    40, barHeight);
            barParams.gravity = Gravity.CENTER_HORIZONTAL;
            bar.setLayoutParams(barParams);

            // Animación de la barra
            animateBar(bar, barHeight, i * 100);

            // Número encima de la barra
            TextView labelValue = new TextView(this);
            labelValue.setText(String.valueOf(lessons));
            labelValue.setTextSize(12);
            labelValue.setTextColor(Color.WHITE);
            labelValue.setGravity(Gravity.CENTER);
            labelValue.setPadding(4, 4, 4, 4);
            labelValue.setBackgroundColor(barColor);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            labelParams.setMargins(0, 0, 0, 8);
            labelValue.setLayoutParams(labelParams);

            if (lessons > 0) {
                barContainer.addView(labelValue);
            }
            barContainer.addView(bar);

            chartContainer.addView(barContainer);

            // Leyenda de días
            TextView dayLabel = new TextView(this);
            dayLabel.setText(day);
            dayLabel.setTextSize(12);
            dayLabel.setTextColor(barColor);
            dayLabel.setTypeface(null, android.graphics.Typeface.BOLD);            dayLabel.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            dayLabel.setLayoutParams(dayParams);

            daysLegend.addView(dayLabel);
        }
    }

    /**
     * Muestra el desglose de lecciones por día
     */
    private void displayLessonsBreakdown(WeeklyStats stats) {
        lessonsBreakdown.removeAllViews();

        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        String[] shortDays = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

        for (int i = 0; i < days.length; i++) {
            WeeklyStats.DayStats dayStats = stats.getWeekData().get(shortDays[i]);
            int lessons = dayStats != null ? dayStats.getLessonsCompleted() : 0;
            long time = dayStats != null ? dayStats.getStudyTimeMinutes() : 0;

            LinearLayout dayRow = new LinearLayout(this);
            dayRow.setOrientation(LinearLayout.HORIZONTAL);
            dayRow.setGravity(Gravity.CENTER_VERTICAL);
            dayRow.setPadding(0, 8, 0, 8);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 4, 0, 4);
            dayRow.setLayoutParams(rowParams);

            // Icono
            TextView icon = new TextView(this);
            icon.setText(lessons > 0 ? "✅" : "⭕");
            icon.setTextSize(16);
            icon.setPadding(0, 0, 12, 0);

            // Día
            TextView dayName = new TextView(this);
            dayName.setText(days[i]);
            dayName.setTextSize(14);
            dayName.setTextColor(ContextCompat.getColor(this, R.color.negro));
            LinearLayout.LayoutParams dayNameParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            dayName.setLayoutParams(dayNameParams);

            // Lecciones
            TextView lessonsCount = new TextView(this);
            lessonsCount.setText(lessons + " lecciones • " + time + " min");
            lessonsCount.setTextSize(12);
            lessonsCount.setTextColor(ContextCompat.getColor(this, R.color.gris));

            dayRow.addView(icon);
            dayRow.addView(dayName);
            dayRow.addView(lessonsCount);

            lessonsBreakdown.addView(dayRow);
        }
    }

    /**
     * Muestra mensaje motivacional según el desempeño
     */
    private void displayMotivationalMessage(WeeklyStats stats) {
        String message;
        int totalLessons = stats.getTotalLessonsCompleted();

        if (totalLessons >= 20) {
            message = "¡Increíble! Eres una superestrella 🌟";
        } else if (totalLessons >= 15) {
            message = "¡Excelente trabajo esta semana! 🎉";
        } else if (totalLessons >= 10) {
            message = "¡Muy bien! Sigue así 💪";
        } else if (totalLessons >= 5) {
            message = "¡Buen progreso! Cada día cuenta 📚";
        } else if (totalLessons > 0) {
            message = "¡Gran comienzo! Sigamos aprendiendo 🚀";
        } else {
            message = "¡Comienza tu aventura de aprendizaje! ✨";
        }

        tvMotivationalMessage.setText(message);
    }

    /**
     * Muestra estadísticas vacías cuando no hay datos
     */
    private void displayEmptyStatistics() {
        tvTotalLessons.setText("0");
        tvTotalTime.setText("0");
        tvTotalExp.setText("0");
        tvMostActiveDay.setText("Ninguno");
        tvAverageDailyTime.setText("0 min");
        tvWeeklyTotalTime.setText("0 min");
        tvMotivationalMessage.setText("¡Comienza tu aventura! ✨");
    }

    /**
     * Anima un número de 0 al valor final
     */
    private void animateNumber(TextView textView, int targetValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(value));
        });

        animator.start();
    }

    /**
     * Anima una barra del gráfico
     */
    private void animateBar(View bar, int targetHeight, long delay) {
        bar.getLayoutParams().height = 0;
        bar.requestLayout();

        bar.postDelayed(() -> {
            ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
            animator.setDuration(600);
            animator.setInterpolator(new BounceInterpolator());

            animator.addUpdateListener(animation -> {
                int height = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = bar.getLayoutParams();
                params.height = height;
                bar.setLayoutParams(params);
            });

            animator.start();
        }, delay);
    }

    /**
     * Anima todas las vistas con fade in
     */
    private void animateViews() {
        View[] views = {
                findViewById(R.id.containerSummary),      // ✅ CardView con ID
                chartContainer,                           // ✅ Ya es un View
                lessonsBreakdown,                         // ✅ Ya es un View
                findViewById(R.id.containerAverageTime)
        };

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            view.setAlpha(0f);
            view.setTranslationY(50f);

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(i * 100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            audioManager.reproducirClic();
            finish();
        });
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_stats;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }
}