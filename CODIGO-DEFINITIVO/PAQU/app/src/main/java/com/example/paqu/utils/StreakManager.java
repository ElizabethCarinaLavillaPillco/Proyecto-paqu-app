package com.example.paqu.utils;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        DatabaseReference userRef = db.child("Usuarios").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    String today = dateFormat.format(new Date());
                    final long[] newStreak = {1}; // Por defecto 1 día

                    if (snapshot.child("streak").exists()) {
                        DataSnapshot streakSnapshot = snapshot.child("streak");
                        String lastDate = streakSnapshot.child("lastActiveDate").getValue(String.class);
                        Long current = streakSnapshot.child("currentStreak").getValue(Long.class);
                        Long longest = streakSnapshot.child("longestStreak").getValue(Long.class);

                        Log.d("STREAK_CALC", "📅 Última vez: " + lastDate + " | Hoy: " + today);

                        if (lastDate != null) {
                            if (lastDate.equals(today)) {
                                // ✅ YA ESTUVO ACTIVO HOY - mantener racha actual
                                newStreak[0] = current != null ? current : 1;
                                Log.d("STREAK_CALC", "🔄 Ya activo hoy - Mantener: " + newStreak[0]);
                            } else {
                                // ✅ VERIFICAR SI FUE AYER
                                Calendar yesterdayCal = Calendar.getInstance();
                                yesterdayCal.add(Calendar.DATE, -1);
                                String yesterday = dateFormat.format(yesterdayCal.getTime());

                                if (lastDate.equals(yesterday)) {
                                    // ✅ AYER SÍ ESTUVO ACTIVO - incrementar racha
                                    newStreak[0] = (current != null ? current : 0) + 1;
                                    Log.d("STREAK_CALC", "🎉 Ayer activo - Incrementar: " + (current != null ? current : 0) + " → " + newStreak[0]);
                                } else {
                                    // ✅ PASÓ MÁS DE 1 DÍA - reiniciar racha
                                    newStreak[0] = 1;
                                    Log.d("STREAK_CALC", "🔄 Pasó más de 1 día - Reiniciar: " + newStreak[0]);
                                }
                            }
                        }
                    } else {
                        // ✅ PRIMERA VEZ DEL USUARIO
                        Log.d("STREAK_CALC", "👤 Primera vez - Iniciar: 1");
                        newStreak[0] = 1;
                    }

                    // ✅ CALCULAR LA RACHA MÁS LARGA
                    long currentLongest = 0;
                    if (snapshot.child("streak").exists()) {
                        Long longest = snapshot.child("streak").child("longestStreak").getValue(Long.class);
                        currentLongest = longest != null ? longest : 0;
                    }
                    long newLongestStreak = Math.max(currentLongest, newStreak[0]);

                    // ✅ GUARDAR EN FIREBASE
                    Map<String, Object> streakData = new HashMap<>();
                    streakData.put("currentStreak", newStreak[0]);
                    streakData.put("lastActiveDate", today);
                    streakData.put("longestStreak", newLongestStreak);

                    userRef.child("streak").setValue(streakData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, DatabaseReference ref) {
                            if (error == null) {
                                Log.d("STREAK", "✅ Racha guardada: " + newStreak[0] + " días (Máxima: " + newLongestStreak + ")");
                                callback.onStreakUpdated((int) newStreak[0]);
                            } else {
                                Log.e("STREAK", "❌ Error guardando: " + error.getMessage());
                                callback.onError(error.getMessage());
                            }
                        }
                    });

                } catch (Exception e) {
                    Log.e("STREAK_ERROR", "Error: " + e.getMessage());
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("STREAK_ERROR", "Database error: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    public interface StreakCallback {
        void onStreakLoaded(int currentStreak, int longestStreak);
        void onError(String error);
    }

    public void getUserStreak(String userId, StreakCallback callback) {
        DatabaseReference streakRef = db.child("Usuarios").child(userId).child("streak");

        streakRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long current = snapshot.child("currentStreak").getValue(Long.class);
                    Long longest = snapshot.child("longestStreak").getValue(Long.class);

                    int currentStreak = current != null ? current.intValue() : 1;
                    int longestStreak = longest != null ? longest.intValue() : 1;

                    Log.d("STREAK_LOAD", "📊 Racha cargada: " + currentStreak + " días");
                    callback.onStreakLoaded(currentStreak, longestStreak);
                } else {
                    Log.d("STREAK_LOAD", "📊 Primera vez - Racha: 1 día");
                    callback.onStreakLoaded(1, 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("STREAK_LOAD", "Error cargando: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }
}