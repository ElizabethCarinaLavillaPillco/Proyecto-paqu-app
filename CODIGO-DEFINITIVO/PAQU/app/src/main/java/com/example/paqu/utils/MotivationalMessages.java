package com.example.paqu.utils;

import java.util.Random;

public class MotivationalMessages {
    private static final Random random = new Random();

    // Mensajes motivacionales
    private static final String[] MOTIVATIONAL = {
            "¡Cada palabra quechua te acerca a tus raíces! 💪",
            "Hoy es un gran día para aprender algo nuevo 🌟",
            "¡Vas por buen camino! Sigue así 🔥",
            "Tu esfuerzo de hoy construye tu mañana 🏔️",
            "¡No pares ahora! Estás haciendo un gran trabajo 🎯",
            "Cada lección te hace más fuerte 💫",
            "¡Sigue así, campeón/a! 🏆",
            "El conocimiento es poder, y tú lo estás consiguiendo 📚"
    };

    // Mensajes culturales
    private static final String[] CULTURAL = {
            "El quechua es la lengua de los incas - ¡preservémosla! 🗻",
            "Aprender quechua es conectar con nuestra historia ancestral",
            "¡Hablar quechua es llevar a los apus en tu voz! ⛰️",
            "Cada palabra quechua es un tesoro cultural ✨",
            "El quechua es música para los oídos 🎵",
            "Nuestros antepasados hablaban quechua - honrémoslos 🙏",
            "El quechua es un puente con nuestra tierra 🏞️"
    };

    // Mensajes de progreso
    private static final String[] PROGRESS = {
            "¡Llevas {streak} días consecutivos! Sigue así 🎉",
            "Racha de {streak} días - ¡Eso es consistencia! ⚡",
            "{streak} días seguidos aprendiendo - ¡Impresionante! 🚀",
            "Mantén tu racha de {streak} días 💎",
            "¡{streak} días sin parar! Eso es dedicación 💫",
            "Vas por el día {streak} - ¡No lo abandones! 🔥"
    };

    // Mensajes de meta diaria
    private static final String[] DAILY_GOAL = {
            "¡Completa tu lección diaria! 🎯",
            "Es hora de practicar quechua 📖",
            "Tu meta diaria te espera 🎯",
            "No olvides tu práctica de hoy 📚",
            "¡Momento de aprender! El quechua te llama 🗣️",
            "5 minutos de quechua hoy marcan la diferencia ⏱️"
    };

    public static String getMessage(int messageType, int currentStreak) {
        switch (messageType) {
            case 0: // Motivacionales y Culturales
                return getMixedMessage(currentStreak);
            case 1: // Solo Motivacionales
                return getMotivationalMessage(currentStreak);
            case 2: // Solo Progreso
                return getProgressMessage(currentStreak);
            case 3: // Todos los tipos
                return getAllTypesMessage(currentStreak);
            default:
                return getDefaultMessage(currentStreak);
        }
    }

    private static String getMixedMessage(int streak) {
        int type = random.nextInt(3);
        switch (type) {
            case 0: return getMotivationalMessage(streak);
            case 1: return getCulturalMessage(streak);
            case 2: return getProgressMessage(streak);
            default: return getDefaultMessage(streak);
        }
    }

    private static String getMotivationalMessage(int streak) {
        String message = MOTIVATIONAL[random.nextInt(MOTIVATIONAL.length)];
        if (streak > 0 && random.nextBoolean()) {
            message += " Llevas " + streak + " días de racha!";
        }
        return message;
    }

    private static String getCulturalMessage(int streak) {
        String message = CULTURAL[random.nextInt(CULTURAL.length)];
        if (streak > 0 && random.nextBoolean()) {
            message += " Ya vas " + streak + " días aprendiendo!";
        }
        return message;
    }

    private static String getProgressMessage(int streak) {
        if (streak > 0) {
            String message = PROGRESS[random.nextInt(PROGRESS.length)];
            return message.replace("{streak}", String.valueOf(streak));
        } else {
            return DAILY_GOAL[random.nextInt(DAILY_GOAL.length)];
        }
    }

    private static String getAllTypesMessage(int streak) {
        int type = random.nextInt(4);
        switch (type) {
            case 0: return getMotivationalMessage(streak);
            case 1: return getCulturalMessage(streak);
            case 2: return getProgressMessage(streak);
            case 3: return DAILY_GOAL[random.nextInt(DAILY_GOAL.length)];
            default: return getDefaultMessage(streak);
        }
    }

    private static String getDefaultMessage(int streak) {
        return "¡Es hora de practicar quechua! 🎯" +
                (streak > 0 ? " Llevas " + streak + " días consecutivos!" : "");
    }

    // Método para obtener título de notificación
    public static String getNotificationTitle(int streak) {
        if (streak <= 0) {
            return "¡Aprende Quechua Hoy! 🏔️";
        } else if (streak == 1) {
            return "¡Primer Día Consecutivo! 🎉";
        } else if (streak < 7) {
            return "¡Sigue Tu Racha! 🔥";
        } else if (streak < 30) {
            return "¡Racha Impresionante! ⚡";
        } else {
            return "¡Leyenda Viviente! 🏆";
        }
    }
}