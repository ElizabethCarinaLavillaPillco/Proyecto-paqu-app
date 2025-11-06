package com.example.paqu.models;

import java.util.List;

/**
 * Modelo para ejercicios del reto contrarreloj
 */
public class TimeAttackExercise {
    public enum ExerciseType {
        TRANSLATE,           // Traducir palabra/frasez
        SELECT_CORRECT,      // Seleccionar palabra correcta
        FORM_SENTENCE,       // Formar oración
        MATCH_PAIRS          // Unir palabras (2 columnas)
    }

    private String id;
    private ExerciseType type;
    private String question;
    private String correctAnswer;
    private List<String> options;
    private int audioResId;
    private int imageResId;
    private String quechuaText;
    private String spanishText;

    // Para ejercicio de unir pares
    private List<String> leftColumn;
    private List<String> rightColumn;
    private List<MatchPair> correctPairs;

    public TimeAttackExercise() {
    }

    // Constructor para ejercicios de traducción
    public TimeAttackExercise(String id, ExerciseType type, String question,
                              String correctAnswer, int audioResId) {
        this.id = id;
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.audioResId = audioResId;
    }

    // Constructor para selección múltiple
    public TimeAttackExercise(String id, ExerciseType type, String question,
                              String correctAnswer, List<String> options,
                              int audioResId, int imageResId) {
        this.id = id;
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.audioResId = audioResId;
        this.imageResId = imageResId;
    }

    // Constructor para formar oraciones
    public TimeAttackExercise(String id, String question, String correctAnswer,
                              List<String> words, int audioResId) {
        this.id = id;
        this.type = ExerciseType.FORM_SENTENCE;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = words;
        this.audioResId = audioResId;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ExerciseType getType() { return type; }
    public void setType(ExerciseType type) { this.type = type; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getAudioResId() { return audioResId; }
    public void setAudioResId(int audioResId) { this.audioResId = audioResId; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getQuechuaText() { return quechuaText; }
    public void setQuechuaText(String quechuaText) { this.quechuaText = quechuaText; }

    public String getSpanishText() { return spanishText; }
    public void setSpanishText(String spanishText) { this.spanishText = spanishText; }

    public List<String> getLeftColumn() { return leftColumn; }
    public void setLeftColumn(List<String> leftColumn) { this.leftColumn = leftColumn; }

    public List<String> getRightColumn() { return rightColumn; }
    public void setRightColumn(List<String> rightColumn) { this.rightColumn = rightColumn; }

    public List<MatchPair> getCorrectPairs() { return correctPairs; }
    public void setCorrectPairs(List<MatchPair> correctPairs) { this.correctPairs = correctPairs; }

    // Clase interna para pares de coincidencias
    public static class MatchPair {
        public String left;
        public String right;

        public MatchPair(String left, String right) {
            this.left = left;
            this.right = right;
        }
    }
}