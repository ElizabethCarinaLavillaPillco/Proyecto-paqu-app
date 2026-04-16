package com.example.paqu.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos para estadísticas semanales del usuario
 */
public class WeeklyStats {
    private Map<String, DayStats> weekData; // "Lun", "Mar", "Mié", etc.
    private int totalLessonsCompleted;
    private long totalStudyTimeMinutes;
    private int currentWeekStreak;
    private int bestWeekStreak;
    private double averageAccuracy;

    public WeeklyStats() {
        this.weekData = new HashMap<>();
        initializeWeekData();
        this.totalLessonsCompleted = 0;
        this.totalStudyTimeMinutes = 0;
        this.currentWeekStreak = 0;
        this.bestWeekStreak = 0;
        this.averageAccuracy = 0.0;
    }

    private void initializeWeekData() {
        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (String day : days) {
            weekData.put(day, new DayStats());
        }
    }

    // Clase interna para estadísticas de cada día
    public static class DayStats {
        private int lessonsCompleted;
        private long studyTimeMinutes;
        private int experienceGained;
        private boolean hasActivity;

        public DayStats() {
            this.lessonsCompleted = 0;
            this.studyTimeMinutes = 0;
            this.experienceGained = 0;
            this.hasActivity = false;
        }

        public DayStats(int lessonsCompleted, long studyTimeMinutes, int experienceGained) {
            this.lessonsCompleted = lessonsCompleted;
            this.studyTimeMinutes = studyTimeMinutes;
            this.experienceGained = experienceGained;
            this.hasActivity = lessonsCompleted > 0 || studyTimeMinutes > 0;
        }

        // Getters y setters
        public int getLessonsCompleted() { return lessonsCompleted; }
        public void setLessonsCompleted(int lessonsCompleted) {
            this.lessonsCompleted = lessonsCompleted;
            this.hasActivity = lessonsCompleted > 0 || studyTimeMinutes > 0;
        }

        public long getStudyTimeMinutes() { return studyTimeMinutes; }
        public void setStudyTimeMinutes(long studyTimeMinutes) {
            this.studyTimeMinutes = studyTimeMinutes;
            this.hasActivity = lessonsCompleted > 0 || studyTimeMinutes > 0;
        }

        public int getExperienceGained() { return experienceGained; }
        public void setExperienceGained(int experienceGained) {
            this.experienceGained = experienceGained;
        }

        public boolean hasActivity() { return hasActivity; }
        public void setHasActivity(boolean hasActivity) {
            this.hasActivity = hasActivity;
        }
    }

    // Getters y setters principales
    public Map<String, DayStats> getWeekData() { return weekData; }
    public void setWeekData(Map<String, DayStats> weekData) { this.weekData = weekData; }

    public int getTotalLessonsCompleted() { return totalLessonsCompleted; }
    public void setTotalLessonsCompleted(int totalLessonsCompleted) {
        this.totalLessonsCompleted = totalLessonsCompleted;
    }

    public long getTotalStudyTimeMinutes() { return totalStudyTimeMinutes; }
    public void setTotalStudyTimeMinutes(long totalStudyTimeMinutes) {
        this.totalStudyTimeMinutes = totalStudyTimeMinutes;
    }

    public int getCurrentWeekStreak() { return currentWeekStreak; }
    public void setCurrentWeekStreak(int currentWeekStreak) {
        this.currentWeekStreak = currentWeekStreak;
    }

    public int getBestWeekStreak() { return bestWeekStreak; }
    public void setBestWeekStreak(int bestWeekStreak) {
        this.bestWeekStreak = bestWeekStreak;
    }

    public double getAverageAccuracy() { return averageAccuracy; }
    public void setAverageAccuracy(double averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }

    /**
     * Obtiene el día con más actividad de la semana
     */
    public String getMostActiveDay() {
        String mostActiveDay = "Ninguno";
        int maxLessons = 0;

        for (Map.Entry<String, DayStats> entry : weekData.entrySet()) {
            if (entry.getValue().getLessonsCompleted() > maxLessons) {
                maxLessons = entry.getValue().getLessonsCompleted();
                mostActiveDay = entry.getKey();
            }
        }

        return mostActiveDay;
    }

    /**
     * Calcula el promedio de tiempo de estudio diario (en minutos)
     */
    public double getAverageDailyStudyTime() {
        int daysWithActivity = 0;
        for (DayStats stats : weekData.values()) {
            if (stats.hasActivity()) {
                daysWithActivity++;
            }
        }

        if (daysWithActivity == 0) return 0.0;
        return (double) totalStudyTimeMinutes / daysWithActivity;
    }

    /**
     * Obtiene el total de experiencia ganada en la semana
     */
    public int getTotalExperienceGained() {
        int totalExp = 0;
        for (DayStats stats : weekData.values()) {
            totalExp += stats.getExperienceGained();
        }
        return totalExp;
    }
}