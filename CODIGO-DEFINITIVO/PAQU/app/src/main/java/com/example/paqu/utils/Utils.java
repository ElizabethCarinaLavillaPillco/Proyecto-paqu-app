package com.example.paqu.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Clase de utilidades generales para la aplicación
 */
public class Utils {

    /**
     * Formatea segundos a formato MM:SS
     */
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }

    /**
     * Formatea timestamp a fecha legible
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Calcula tiempo restante hasta la próxima recarga de vidas
     */
    public static String getTimeUntilHeartRefresh(long nextRefreshTimestamp) {
        long now = System.currentTimeMillis();
        long diff = nextRefreshTimestamp - now;

        if (diff <= 0) {
            return "Ahora";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%dm", minutes);
        }
    }

    /**
     * Verifica si dos fechas son del mismo día
     */
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date1 = sdf.format(new Date(timestamp1));
        String date2 = sdf.format(new Date(timestamp2));
        return date1.equals(date2);
    }

    /**
     * Muestra un Toast con duración corta
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra un Toast con duración larga
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Animación de escala (bounce)
     */
    public static void animateBounce(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);

        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());

        scaleX.start();
        scaleY.start();
    }

    /**
     * Animación de pulso
     */
    public static void animatePulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);

        scaleX.start();
        scaleY.start();
    }

    /**
     * Animación de shake (sacudir)
     */
    public static void animateShake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Animación fade in
     */
    public static void animateFadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(300);
        animator.start();
    }

    /**
     * Animación fade out
     */
    public static void animateFadeOut(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(300);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    /**
     * Animación slide up
     */
    public static void animateSlideUp(View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY",
                view.getHeight(), 0);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Animación slide down
     */
    public static void animateSlideDown(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY",
                0, view.getHeight());
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    /**
     * Calcula el nivel basado en la experiencia
     */
    public static int calculateLevel(int experience) {
        // Formula: level = sqrt(experience / 100)
        return (int) Math.floor(Math.sqrt(experience / 100.0)) + 1;
    }

    /**
     * Calcula la experiencia necesaria para el siguiente nivel
     */
    public static int getExpForNextLevel(int currentLevel) {
        // Formula: exp = (level^2) * 100
        return currentLevel * currentLevel * 100;
    }

    /**
     * Calcula el porcentaje de progreso hacia el siguiente nivel
     */
    public static int getLevelProgress(int experience) {
        int currentLevel = calculateLevel(experience);
        int expForCurrentLevel = (currentLevel - 1) * (currentLevel - 1) * 100;
        int expForNextLevel = getExpForNextLevel(currentLevel);
        int expInLevel = experience - expForCurrentLevel;
        int expNeeded = expForNextLevel - expForCurrentLevel;

        return (int) ((expInLevel * 100.0) / expNeeded);
    }

    /**
     * Valida si una respuesta es correcta (ignorando mayúsculas y espacios extras)
     */
    public static boolean validateAnswer(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        String normalized1 = userAnswer.trim().toLowerCase().replaceAll("\\s+", " ");
        String normalized2 = correctAnswer.trim().toLowerCase().replaceAll("\\s+", " ");

        return normalized1.equals(normalized2);
    }

    /**
     * Calcula el porcentaje de similitud entre dos strings (para respuestas parciales)
     */
    public static int calculateSimilarity(String s1, String s2) {
        String longer = s1;
        String shorter = s2;

        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }

        int longerLength = longer.length();
        if (longerLength == 0) {
            return 100;
        }

        int distance = editDistance(longer, shorter);
        return (int) Math.round((1.0 - distance / (double) longerLength) * 100.0);
    }

    /**
     * Calcula la distancia de Levenshtein entre dos strings
     */
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    /**
     * Genera un color aleatorio para avatares
     */
    public static int getRandomColor() {
        int[] colors = {
                0xFFE57373, // Rojo
                0xFF81C784, // Verde
                0xFF64B5F6, // Azul
                0xFFFFD54F, // Amarillo
                0xFFBA68C8, // Púrpura
                0xFF4DD0E1, // Cian
                0xFFFF8A65  // Naranja
        };
        return colors[(int) (Math.random() * colors.length)];
    }

    /**
     * Convierte milisegundos a texto legible
     */
    public static String millisToReadableTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Verifica si el dispositivo tiene conexión a internet
     */
    public static boolean isNetworkAvailable(Context context) {
        android.net.ConnectivityManager cm =
                (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Genera un mensaje motivacional basado en el porcentaje de aciertos
     */
    public static String getMotivationalMessage(int accuracyPercent) {
        if (accuracyPercent == 100) {
            return "¡Perfecto! ¡Eres increíble! 🌟";
        } else if (accuracyPercent >= 90) {
            return "¡Excelente trabajo! 🎉";
        } else if (accuracyPercent >= 80) {
            return "¡Muy bien! Sigue así 👏";
        } else if (accuracyPercent >= 70) {
            return "¡Buen esfuerzo! 💪";
        } else if (accuracyPercent >= 60) {
            return "Vas mejorando, sigue practicando 📚";
        } else {
            return "No te rindas, ¡tú puedes! 🔥";
        }
    }

    /**
     * Obtiene emoji según el nivel de racha
     */
    public static String getStreakEmoji(int streakDays) {
        if (streakDays >= 30) {
            return "🔥🔥🔥";
        } else if (streakDays >= 14) {
            return "🔥🔥";
        } else if (streakDays >= 7) {
            return "🔥";
        } else if (streakDays >= 3) {
            return "⭐";
        } else {
            return "✨";
        }
    }
}