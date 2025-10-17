package com.example.paqu.managers;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado para todas las operaciones de Firebase
 * Facilita el mantenimiento y migración futura
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final DatabaseReference database;

    // Referencias constantes
    private static final String USERS_PATH = "users";
    private static final String USER_LESSONS_PATH = "user_lessons";
    private static final String LESSONS_PATH = "lessons";

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // ==================== USER INFO ====================

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void getUserProgress(String userId, ProgressCallback callback) {
        database.child(USERS_PATH).child(userId).child("progress")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserProgress progress = new UserProgress();
                        progress.hearts = snapshot.child("hearts").getValue(Integer.class);
                        progress.coins = snapshot.child("coins").getValue(Integer.class);
                        progress.currentStreak = snapshot.child("currentStreak").getValue(Integer.class);
                        progress.experience = snapshot.child("experience").getValue(Integer.class);
                        progress.level = snapshot.child("level").getValue(Integer.class);
                        progress.nextHeartRefresh = snapshot.child("nextHeartRefresh").getValue(Long.class);

                        // Valores por defecto
                        if (progress.hearts == null) progress.hearts = 5;
                        if (progress.coins == null) progress.coins = 0;
                        if (progress.currentStreak == null) progress.currentStreak = 0;
                        if (progress.experience == null) progress.experience = 0;
                        if (progress.level == null) progress.level = 1;

                        callback.onSuccess(progress);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ==================== VIDAS ====================

    public void checkAndRefillHearts(String userId, HeartsCallback callback) {
        DatabaseReference userRef = database.child(USERS_PATH).child(userId).child("progress");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer hearts = snapshot.child("hearts").getValue(Integer.class);
                Long nextRefresh = snapshot.child("nextHeartRefresh").getValue(Long.class);

                if (hearts == null) hearts = 5;
                long now = System.currentTimeMillis();

                // Si tiene menos de 5 vidas, verificar si es hora de recargar
                if (hearts < 5 && nextRefresh != null && now >= nextRefresh) {
                    // Recargar 1 vida cada 30 minutos (1800000 ms)
                    long timePassed = now - nextRefresh;
                    int heartsToAdd = (int) (timePassed / 1800000) + 1;
                    hearts = Math.min(5, hearts + heartsToAdd);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("hearts", hearts);
                    if (hearts >= 5) {
                        updates.put("nextHeartRefresh", null);
                    } else {
                        updates.put("nextHeartRefresh", now + 1800000);
                    }

                    userRef.updateChildren(updates);
                }

                callback.onHeartsChecked(hearts, nextRefresh);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void loseHeart(String userId, HeartLostCallback callback) {
        DatabaseReference progressRef = database.child(USERS_PATH).child(userId).child("progress");

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer hearts = snapshot.child("hearts").getValue(Integer.class);
                if (hearts == null) hearts = 5;

                hearts--;
                Map<String, Object> updates = new HashMap<>();
                updates.put("hearts", hearts);

                // Si llega a 0, establecer tiempo de recarga
                if (hearts == 0) {
                    updates.put("nextHeartRefresh", System.currentTimeMillis() + 1800000);
                }

                Integer finalHearts = hearts;
                progressRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onHeartLost(finalHearts);
                    } else {
                        callback.onError("Error al actualizar vidas");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== DIAMANTES ====================

    public void useCoinsForHearts(String userId, CoinsCallback callback) {
        DatabaseReference progressRef = database.child(USERS_PATH).child(userId).child("progress");

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer coins = snapshot.child("coins").getValue(Integer.class);
                if (coins == null) coins = 0;

                final int HEARTS_COST = 100;

                if (coins >= HEARTS_COST) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("coins", coins - HEARTS_COST);
                    updates.put("hearts", 5);
                    updates.put("nextHeartRefresh", null);

                    Integer finalCoins = coins;
                    progressRef.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onCoinsUsed(finalCoins - HEARTS_COST);
                        } else {
                            callback.onError("Error al usar diamantes");
                        }
                    });
                } else {
                    callback.onInsufficientCoins(coins);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== RACHA (STREAK) ====================

    public void updateStreak(String userId, StreakCallback callback) {
        DatabaseReference progressRef = database.child(USERS_PATH).child(userId).child("progress");

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastActivityTimestamp = snapshot.child("lastActivityDate").getValue(Long.class);
                Integer currentStreak = snapshot.child("currentStreak").getValue(Integer.class);
                Integer longestStreak = snapshot.child("longestStreak").getValue(Integer.class);

                if (currentStreak == null) currentStreak = 0;
                if (longestStreak == null) longestStreak = 0;

                long now = System.currentTimeMillis();
                long oneDayMs = 86400000; // 24 horas

                boolean shouldUpdateStreak = false;

                if (lastActivityTimestamp == null) {
                    // Primera vez
                    currentStreak = 1;
                    shouldUpdateStreak = true;
                } else {
                    long timeDiff = now - lastActivityTimestamp;

                    if (timeDiff < oneDayMs) {
                        // Mismo día, no actualizar
                        shouldUpdateStreak = false;
                    } else if (timeDiff < oneDayMs * 2) {
                        // Día consecutivo
                        currentStreak++;
                        shouldUpdateStreak = true;
                    } else {
                        // Racha rota
                        currentStreak = 1;
                        shouldUpdateStreak = true;
                    }
                }

                if (shouldUpdateStreak) {
                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("currentStreak", currentStreak);
                    updates.put("longestStreak", longestStreak);
                    updates.put("lastActivityDate", now);

                    int finalCurrentStreak = currentStreak;
                    boolean finalShouldUpdateStreak = shouldUpdateStreak;
                    progressRef.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onStreakUpdated(finalCurrentStreak, finalShouldUpdateStreak);
                        }
                    });
                } else {
                    callback.onStreakUpdated(currentStreak, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== COMPLETAR LECCIÓN ====================

    public void completeLesson(String userId, String lessonId, LessonResult result, CompletionCallback callback) {
        String userLessonKey = userId + "_" + lessonId;
        DatabaseReference lessonRef = database.child(USER_LESSONS_PATH).child(userLessonKey);

        Map<String, Object> lessonData = new HashMap<>();

        // Progress
        Map<String, Object> progress = new HashMap<>();
        progress.put("userId", userId);
        progress.put("lessonId", lessonId);
        progress.put("completed", result.completed);
        progress.put("score", result.score);
        progress.put("timeSpent", result.timeSpent);
        progress.put("attempts", result.attempts);
        progress.put("perfect", result.perfect);
        progress.put("completedAt", System.currentTimeMillis());
        progress.put("mistakes", result.mistakes);

        lessonData.put("progress", progress);

        // Actualizar progreso del usuario
        DatabaseReference userProgressRef = database.child(USERS_PATH).child(userId).child("progress");

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentExp = snapshot.child("experience").getValue(Integer.class);
                Integer currentCoins = snapshot.child("coins").getValue(Integer.class);
                Integer totalLessons = snapshot.child("totalLessons").getValue(Integer.class);

                if (currentExp == null) currentExp = 0;
                if (currentCoins == null) currentCoins = 0;
                if (totalLessons == null) totalLessons = 0;

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("experience", currentExp + result.expGained);
                userUpdates.put("coins", currentCoins + result.coinsGained);
                userUpdates.put("totalLessons", totalLessons + 1);

                // Guardar lección y actualizar usuario
                lessonRef.setValue(lessonData);
                userProgressRef.updateChildren(userUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onLessonCompleted(result);
                    } else {
                        callback.onError("Error al guardar progreso");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ==================== CALLBACKS ====================

    public interface ProgressCallback {
        void onSuccess(UserProgress progress);
        void onError(String error);
    }

    public interface HeartsCallback {
        void onHeartsChecked(int hearts, Long nextRefresh);
        void onError(String error);
    }

    public interface HeartLostCallback {
        void onHeartLost(int remainingHearts);
        void onError(String error);
    }

    public interface CoinsCallback {
        void onCoinsUsed(int remainingCoins);
        void onInsufficientCoins(int currentCoins);
        void onError(String error);
    }

    public interface StreakCallback {
        void onStreakUpdated(int currentStreak, boolean wasIncremented);
        void onError(String error);
    }

    public interface CompletionCallback {
        void onLessonCompleted(LessonResult result);
        void onError(String error);
    }

    // ==================== MODELOS ====================

    public static class UserProgress {
        public Integer hearts;
        public Integer coins;
        public Integer currentStreak;
        public Integer experience;
        public Integer level;
        public Long nextHeartRefresh;
    }

    public static class LessonResult {
        public boolean completed;
        public double score;
        public int timeSpent;
        public int attempts;
        public boolean perfect;
        public int expGained;
        public int coinsGained;
        public String[] mistakes;
    }
}