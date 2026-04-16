package com.example.paqu.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.paqu.R;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private static final String CHANNEL_ID = "paqu_daily_reminders";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "🎯 WorkManager ejecutando notificación");

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);

            // Verificar si las notificaciones están activadas
            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
            if (!notificationsEnabled) {
                Log.d(TAG, "Notificaciones desactivadas - cancelando trabajo");
                return Result.success();
            }

            // Obtener configuración
            int messageType = prefs.getInt("message_type", 0);
            int currentStreak = 5; // Por ahora estático

            // Mostrar notificación
            showNotification(messageType, currentStreak);

            Log.d(TAG, "✅ Notificación mostrada exitosamente");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error en NotificationWorker: " + e.getMessage());
            return Result.failure();
        }
    }

    private void showNotification(int messageType, int currentStreak) {
        createNotificationChannel();

        // Obtener mensajes
        String title = MotivationalMessages.getNotificationTitle(currentStreak);
        String message = MotivationalMessages.getMessage(messageType, currentStreak);

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.fuegoicono)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mostrar
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios PAQU",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Recordatorios para practicar quechua");

            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}