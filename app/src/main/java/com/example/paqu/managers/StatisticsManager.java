package com.example.paqu.managers;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.paqu.models.WeeklyStats;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manager para gestionar estadísticas semanales del usuario
 */
public class StatisticsManager {
    private static final String TAG = "StatisticsManager";
    private DatabaseReference database;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dayFormat;

    public StatisticsManager() {
        database = FirebaseDatabase.getInstance().getReference();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dayFormat = new SimpleDateFormat("EEE", Locale.forLanguageTag("es"));
    }

    // Interfaces de callback
    public interface StatsCallback {
        void onSuccess(WeeklyStats stats);
        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Obtiene las estadísticas de la semana actual
     */
    public void getWeeklyStats(String userId, StatsCallback callback) {
        Log.d(TAG, "📊 Obteniendo estadísticas para: " + userId);

        // Obtener rango de fechas de la semana actual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String weekStart = dateFormat.format(calendar.getTime());

        DatabaseReference statsRef = database.child("users").child(userId).child("statistics");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WeeklyStats stats = new WeeklyStats();

                if (snapshot.exists()) {
                    // Cargar datos de cada día de la semana
                    String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                    for (String day : days) {
                        String dateKey = dateFormat.format(dayCalendar.getTime());
                        DataSnapshot daySnapshot = snapshot.child("daily").child(dateKey);

                        if (daySnapshot.exists()) {
                            int lessons = daySnapshot.child("lessonsCompleted").getValue(Integer.class) != null
                                    ? daySnapshot.child("lessonsCompleted").getValue(Integer.class) : 0;
                            long studyTime = daySnapshot.child("studyTimeMinutes").getValue(Long.class) != null
                                    ? daySnapshot.child("studyTimeMinutes").getValue(Long.class) : 0;
                            int exp = daySnapshot.child("experienceGained").getValue(Integer.class) != null
                                    ? daySnapshot.child("experienceGained").getValue(Integer.class) : 0;

                            WeeklyStats.DayStats dayStats = new WeeklyStats.DayStats(lessons, studyTime, exp);
                            stats.getWeekData().put(day, dayStats);
                        }

                        dayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    // Cargar totales
                    DataSnapshot weekSnapshot = snapshot.child("weekly").child(weekStart);
                    if (weekSnapshot.exists()) {
                        Integer totalLessons = weekSnapshot.child("totalLessonsCompleted").getValue(Integer.class);
                        Long totalTime = weekSnapshot.child("totalStudyTimeMinutes").getValue(Long.class);
                        Integer weekStreak = weekSnapshot.child("currentWeekStreak").getValue(Integer.class);
                        Integer bestStreak = weekSnapshot.child("bestWeekStreak").getValue(Integer.class);
                        Double avgAccuracy = weekSnapshot.child("averageAccuracy").getValue(Double.class);

                        stats.setTotalLessonsCompleted(totalLessons != null ? totalLessons : 0);
                        stats.setTotalStudyTimeMinutes(totalTime != null ? totalTime : 0);
                        stats.setCurrentWeekStreak(weekStreak != null ? weekStreak : 0);
                        stats.setBestWeekStreak(bestStreak != null ? bestStreak : 0);
                        stats.setAverageAccuracy(avgAccuracy != null ? avgAccuracy : 0.0);
                    }
                }

                Log.d(TAG, "✅ Estadísticas cargadas: " + stats.getTotalLessonsCompleted() + " lecciones");
                callback.onSuccess(stats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al cargar estadísticas: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Actualiza las estadísticas después de completar una lección
     */
    public void updateLessonStats(String userId, long studyTimeSeconds, int expGained, UpdateCallback callback) {
        Log.d(TAG, "📝 Actualizando estadísticas de lección");

        String today = dateFormat.format(Calendar.getInstance().getTime());
        String dayName = dayFormat.format(Calendar.getInstance().getTime());

        // Capitalizar primera letra del día
        dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1).toLowerCase();

        DatabaseReference userStatsRef = database.child("users").child(userId).child("statistics");

        // Actualizar estadísticas diarias
        DatabaseReference dailyRef = userStatsRef.child("daily").child(today);

        dailyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentLessons = 0;
                long currentTime = 0;
                int currentExp = 0;

                if (snapshot.exists()) {
                    Integer lessons = snapshot.child("lessonsCompleted").getValue(Integer.class);
                    Long time = snapshot.child("studyTimeMinutes").getValue(Long.class);
                    Integer exp = snapshot.child("experienceGained").getValue(Integer.class);

                    currentLessons = lessons != null ? lessons : 0;
                    currentTime = time != null ? time : 0;
                    currentExp = exp != null ? exp : 0;
                }

                // Actualizar valores
                Map<String, Object> dailyUpdates = new HashMap<>();
                dailyUpdates.put("lessonsCompleted", currentLessons + 1);
                dailyUpdates.put("studyTimeMinutes", currentTime + (studyTimeSeconds / 60));
                dailyUpdates.put("experienceGained", currentExp + expGained);
                dailyUpdates.put("lastUpdated", System.currentTimeMillis());

                dailyRef.updateChildren(dailyUpdates);

                // Actualizar totales semanales
                updateWeeklyTotals(userId, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al actualizar estadísticas: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Actualiza los totales semanales
     */
    private void updateWeeklyTotals(String userId, UpdateCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String weekStart = dateFormat.format(calendar.getTime());

        DatabaseReference weeklyRef = database.child("users").child(userId)
                .child("statistics").child("weekly").child(weekStart);

        getWeeklyStats(userId, new StatsCallback() {
            @Override
            public void onSuccess(WeeklyStats stats) {
                Map<String, Object> weeklyUpdates = new HashMap<>();
                weeklyUpdates.put("totalLessonsCompleted", stats.getTotalLessonsCompleted());
                weeklyUpdates.put("totalStudyTimeMinutes", stats.getTotalStudyTimeMinutes());
                weeklyUpdates.put("currentWeekStreak", calculateWeekStreak(stats));
                weeklyUpdates.put("lastUpdated", System.currentTimeMillis());

                weeklyRef.updateChildren(weeklyUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Totales semanales actualizados");
                        callback.onSuccess();
                    } else {
                        Log.e(TAG, "❌ Error al actualizar totales semanales");
                        callback.onError("Error al actualizar totales");
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Calcula la racha de días consecutivos de la semana
     */
    private int calculateWeekStreak(WeeklyStats stats) {
        int streak = 0;
        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

        for (String day : days) {
            WeeklyStats.DayStats dayStats = stats.getWeekData().get(day);
            if (dayStats != null && dayStats.hasActivity()) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Reinicia las estadísticas semanales (útil para el inicio de una nueva semana)
     */
    public void resetWeeklyStats(String userId, UpdateCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String weekStart = dateFormat.format(calendar.getTime());

        DatabaseReference weeklyRef = database.child("users").child(userId)
                .child("statistics").child("weekly").child(weekStart);

        weeklyRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "✅ Estadísticas semanales reiniciadas");
                callback.onSuccess();
            } else {
                Log.e(TAG, "❌ Error al reiniciar estadísticas");
                callback.onError("Error al reiniciar estadísticas");
            }
        });
    }
}