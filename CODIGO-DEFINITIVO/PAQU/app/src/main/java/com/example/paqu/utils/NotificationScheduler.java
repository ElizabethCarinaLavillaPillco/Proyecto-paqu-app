package com.example.paqu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";

    public static void scheduleDailyNotifications(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);

            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
            if (!notificationsEnabled) {
                cancelAllNotifications(context);
                Log.d(TAG, "Notificaciones desactivadas - cancelando trabajos");
                return;
            }

            Log.d(TAG, "Programando notificaciones con WorkManager...");

            // Cancelar trabajos existentes
            cancelAllNotifications(context);

            // Programar notificaciones para cada horario activado
            if (prefs.getBoolean("morning_enabled", false)) {
                scheduleNotificationForTime(context, "morning", prefs.getString("morning_time", "8:00 AM"));
            }

            if (prefs.getBoolean("afternoon_enabled", false)) {
                scheduleNotificationForTime(context, "afternoon", prefs.getString("afternoon_time", "2:00 PM"));
            }

            if (prefs.getBoolean("evening_enabled", false)) {
                scheduleNotificationForTime(context, "evening", prefs.getString("evening_time", "7:00 PM"));
            }

            Log.d(TAG, "✅ Todas las notificaciones programadas con WorkManager");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error programando notificaciones: " + e.getMessage());
        }
    }

    private static void scheduleNotificationForTime(Context context, String timeKey, String timeString) {
        try {
            int[] timeParts = parseTimeString(timeString);
            if (timeParts == null) return;

            int hour = timeParts[0];
            int minute = timeParts[1];

            // Calcular delay hasta la próxima ejecución
            long initialDelay = calculateInitialDelay(hour, minute);

            // Crear restricciones (opcional)
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build();

            // Crear trabajo periódico (cada 24 horas)
            PeriodicWorkRequest notificationWork =
                    new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                            .setConstraints(constraints)
                            .build();

            // Programar el trabajo
            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                            "notification_" + timeKey,
                            ExistingPeriodicWorkPolicy.REPLACE,
                            notificationWork
                    );

            Log.d(TAG, "✅ Notificación programada: " + timeKey + " a las " + timeString +
                    " (delay: " + initialDelay + "ms)");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error programando " + timeKey + ": " + e.getMessage());
        }
    }

    private static long calculateInitialDelay(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();

        targetTime.set(Calendar.HOUR_OF_DAY, targetHour);
        targetTime.set(Calendar.MINUTE, targetMinute);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        // Si la hora ya pasó hoy, programar para mañana
        if (targetTime.before(now)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        return targetTime.getTimeInMillis() - now.getTimeInMillis();
    }

    private static int[] parseTimeString(String timeString) {
        try {
            // Formato: "8:00 AM" o "2:00 PM"
            String time = timeString.replace(" AM", "").replace(" PM", "");
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());

            // Convertir a 24h
            if (timeString.contains("PM") && hour != 12) {
                hour += 12;
            } else if (timeString.contains("AM") && hour == 12) {
                hour = 0;
            }

            return new int[]{hour, minute};
        } catch (Exception e) {
            Log.e(TAG, "Error parseando hora: " + timeString);
            return null;
        }
    }

    public static void cancelAllNotifications(Context context) {
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            workManager.cancelUniqueWork("notification_morning");
            workManager.cancelUniqueWork("notification_afternoon");
            workManager.cancelUniqueWork("notification_evening");
            Log.d(TAG, "❌ Todos los trabajos cancelados");
        } catch (Exception e) {
            Log.e(TAG, "Error cancelando trabajos: " + e.getMessage());
        }
    }

    // ✅ Probar notificación inmediata
    public static void testNotificationNow(Context context) {
        try {
            // Crear un trabajo único para prueba inmediata
            androidx.work.OneTimeWorkRequest testWork =
                    new androidx.work.OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .build();

            WorkManager.getInstance(context).enqueue(testWork);
            Log.d(TAG, "🔔 Notificación de prueba enviada via WorkManager");
        } catch (Exception e) {
            Log.e(TAG, "Error en prueba: " + e.getMessage());
        }
    }
}