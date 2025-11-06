package com.example.paqu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.paqu.utils.MotivationalMessages;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "paqu_daily_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "📢 Notificación recibida - Mostrando...");

        int messageType = intent.getIntExtra("message_type", 0);
        int currentStreak = 5; // Valor de prueba

        showNotification(context, messageType, currentStreak);
    }

    private void showNotification(Context context, int messageType, int currentStreak) {
        createNotificationChannel(context);

        // Obtener mensajes dinámicos
        String title = MotivationalMessages.getNotificationTitle(currentStreak);
        String message = MotivationalMessages.getMessage(messageType, currentStreak);

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.fuegoicono) // Usa tu icono de fuego
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mostrar notificación
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Usar ID único para cada notificación
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "✅ Notificación mostrada: " + title);
        } else {
            Log.e(TAG, "❌ Error: NotificationManager es null");
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios PAQU",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Recordatorios para practicar quechua");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Canal de notificación creado");
            }
        }
    }
}