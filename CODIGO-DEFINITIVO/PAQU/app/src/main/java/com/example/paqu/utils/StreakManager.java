package com.example.paqu.utils;

import android.util.Log;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StreakManager {
    private DatabaseReference db;
    private SimpleDateFormat dateFormat;

    public StreakManager() {
        db = FirebaseDatabase.getInstance().getReference();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public interface StreakUpdateCallback {
        void onStreakUpdated(int newStreak);
        void onError(String error);
    }

    public void updateUserStreak(String userId, StreakUpdateCallback callback) {
        Log.d("STREAK_FINAL", "🎯 INICIANDO RACHA para: " + userId);

        String today = dateFormat.format(new Date());

        // Datos de racha
        Map<String, Object> streakData = new HashMap<>();
        streakData.put("currentStreak", 1);
        streakData.put("lastActiveDate", today);
        streakData.put("longestStreak", 1);

        // Guardar en users/{userId}/streak
        DatabaseReference userRef = db.child("users").child(userId).child("streak");

        userRef.setValue(streakData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    Log.d("STREAK_FINAL", "✅ ÉXITO - Racha 1 guardada en users/streak");
                    callback.onStreakUpdated(1);
                } else {
                    Log.e("STREAK_FINAL", "❌ Error users/streak: " + error.getMessage());
                    callback.onError("Permisos denegados: " + error.getMessage());
                }
            }
        });
    }

    public interface StreakCallback {
        void onStreakLoaded(int currentStreak, int longestStreak);
        void onError(String error);
    }

    public void getUserStreak(String userId, StreakCallback callback) {
        DatabaseReference streakRef = db.child("users").child(userId).child("streak");

        streakRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long current = snapshot.child("currentStreak").getValue(Long.class);
                    int currentStreak = current != null ? current.intValue() : 1;
                    Log.d("STREAK_FINAL", "✅ Racha cargada: " + currentStreak);
                    callback.onStreakLoaded(currentStreak, currentStreak);
                } else {
                    Log.d("STREAK_FINAL", "📭 No existe streak, usar 1");
                    callback.onStreakLoaded(1, 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("STREAK_FINAL", "❌ Error cargando: " + error.getMessage());
                callback.onStreakLoaded(1, 1); // Siempre devolver 1 si hay error
            }
        });
    }
}