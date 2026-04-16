package com.example.paqu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

public class SpacedRepetitionManager {
    private SharedPreferences preferences;
    private Gson gson;
    private static final String PREF_NAME = "spaced_repetition_data";
    private static final String KEY_CARDS = "user_cards";

    public SpacedRepetitionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        initializeDefaultCards();
    }

    private void initializeDefaultCards() {
        if (!preferences.contains(KEY_CARDS)) {
            List<ReviewCard> defaultCards = createDefaultCards();
            saveCards(defaultCards);
        }
    }

    private List<ReviewCard> createDefaultCards() {
        List<ReviewCard> cards = new ArrayList<>();

        // 📚 BANCO AMPLIO DE PALABRAS QUECHUA-ESPAÑOL POR DIFICULTAD

        // 🔵 FÁCIL - Saludos y Básicos
        cards.add(new ReviewCard("1", "Allillanchu", "Hola", "easy", "saludos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("2", "Tupananchikkama", "Adiós", "easy", "saludos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("3", "Yusulpayki", "Gracias", "easy", "cortesía", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("4", "Allichu", "Por favor", "easy", "cortesía", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("5", "Arí", "Sí", "easy", "básicos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("6", "Manan", "No", "easy", "básicos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("7", "Allin p'unchay", "Buenos días", "easy", "saludos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("8", "Allin ch'isi", "Buenas tardes", "easy", "saludos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("9", "Allin tuta", "Buenas noches", "easy", "saludos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("10", "Imaynallan kachkanki?", "¿Cómo estás?", "easy", "saludos", 1, 2.5, System.currentTimeMillis()));

        // 🟡 MEDIO - Familia y Personas
        cards.add(new ReviewCard("11", "Ayllu", "Familia", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("12", "Mama", "Madre", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("13", "Tayta", "Padre", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("14", "Wawqi", "Hermano", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("15", "Ñaña", "Hermana", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("16", "Achachi", "Abuelo", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("17", "Awicha", "Abuela", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("18", "Churi", "Hijo", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("19", "Ususi", "Hija", "medium", "familia", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("20", "Masi", "Amigo", "medium", "social", 1, 2.5, System.currentTimeMillis()));

        // 🔴 DIFÍCIL - Naturaleza y Alimentos
        cards.add(new ReviewCard("21", "Yaku", "Agua", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("22", "Nina", "Fuego", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("23", "Allpa", "Tierra", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("24", "Inti", "Sol", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("25", "Killa", "Luna", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("26", "Urqu", "Montaña", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("27", "Mayu", "Río", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("28", "Para", "Lluvia", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("29", "Wayra", "Viento", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("30", "Puyu", "Nube", "hard", "naturaleza", 1, 2.5, System.currentTimeMillis()));

        // 🟣 ALIMENTOS - Vocabulario útil
        cards.add(new ReviewCard("31", "Mikhuna", "Comida", "medium", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("32", "Papa", "Papa", "easy", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("33", "Sara", "Maíz", "medium", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("34", "Arrus", "Arroz", "easy", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("35", "Aycha", "Carne", "medium", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("36", "Challwa", "Pescado", "hard", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("37", "Ruru", "Fruta", "medium", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("38", "T'anta", "Pan", "easy", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("39", "Leche", "Leche", "easy", "alimentos", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("40", "Runtu", "Huevo", "medium", "alimentos", 1, 2.5, System.currentTimeMillis()));

        // 🔵 NÚMEROS - Del 1 al 10
        cards.add(new ReviewCard("41", "Huk", "Uno", "easy", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("42", "Iskay", "Dos", "easy", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("43", "Kinsa", "Tres", "easy", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("44", "Tawa", "Cuatro", "easy", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("45", "Pichqa", "Cinco", "medium", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("46", "Suqta", "Seis", "medium", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("47", "Qanchis", "Siete", "medium", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("48", "Pusaq", "Ocho", "hard", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("49", "Isqun", "Nueve", "hard", "números", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("50", "Chunka", "Diez", "medium", "números", 1, 2.5, System.currentTimeMillis()));

        // 🟡 COLORES - Básicos
        cards.add(new ReviewCard("51", "Puka", "Rojo", "easy", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("52", "Anqas", "Azul", "medium", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("53", "Q'omer", "Verde", "medium", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("54", "Q'illu", "Amarillo", "easy", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("55", "Yuraq", "Blanco", "easy", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("56", "Yana", "Negro", "easy", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("57", "Uqi", "Gris", "hard", "colores", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("58", "Ch'umpi", "Marrón", "hard", "colores", 1, 2.5, System.currentTimeMillis()));

        // 🔴 ANIMALES - Comunes
        cards.add(new ReviewCard("59", "Allqu", "Perro", "easy", "animales", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("60", "Michi", "Gato", "easy", "animales", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("61", "Waka", "Vaca", "medium", "animales", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("62", "Kawallu", "Caballo", "medium", "animales", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("63", "Pisqu", "Pájaro", "medium", "animales", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("64", "Uwija", "Oveja", "hard", "animales", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("65", "Qwi", "Conejo", "hard", "animales", 1, 2.5, System.currentTimeMillis()));

        // 🟣 PARTES DEL CUERPO
        cards.add(new ReviewCard("66", "Uma", "Cabeza", "easy", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("67", "Maki", "Mano", "easy", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("68", "Chaki", "Pie", "easy", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("69", "Ñawi", "Ojo", "medium", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("70", "Simi", "Boca", "medium", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("71", "Sinqa", "Nariz", "medium", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("72", "Ninri", "Oreja", "medium", "cuerpo", 1, 2.5, System.currentTimeMillis()));
        cards.add(new ReviewCard("73", "Sunqu", "Corazón", "hard", "cuerpo", 1, 2.5, System.currentTimeMillis()));

        return cards;
    }

    public List<ReviewCard> getTodaysReviewCards() {
        List<ReviewCard> allCards = getCards();
        List<ReviewCard> todaysCards = new ArrayList<>();

        long currentTime = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;

        for (ReviewCard card : allCards) {
            if (card.getNextReview() <= currentTime) {
                todaysCards.add(card);
            }
        }

        Collections.shuffle(todaysCards);
        return todaysCards;
    }

    public void rateCard(String cardId, String difficulty) {
        List<ReviewCard> allCards = getCards();

        for (ReviewCard card : allCards) {
            if (card.getId().equals(cardId)) {
                int newInterval;
                double newEaseFactor = card.getEaseFactor();

                switch (difficulty) {
                    case "hard":
                        newInterval = 1;
                        newEaseFactor = Math.max(1.3, card.getEaseFactor() - 0.2);
                        break;
                    case "medium":
                        newInterval = (int) (card.getInterval() * 1.5);
                        newEaseFactor = card.getEaseFactor();
                        break;
                    case "easy":
                        newInterval = (int) (card.getInterval() * card.getEaseFactor());
                        newEaseFactor = card.getEaseFactor() + 0.1;
                        break;
                    default:
                        newInterval = card.getInterval();
                }

                card.setInterval(newInterval);
                card.setEaseFactor(newEaseFactor);
                card.setNextReview(System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000));
                break;
            }
        }

        saveCards(allCards);
    }

    public int getTotalCards() {
        return getCards().size();
    }

    public int getDueCardsCount() {
        return getTodaysReviewCards().size();
    }

    // Métodos de persistencia
    private List<ReviewCard> getCards() {
        String json = preferences.getString(KEY_CARDS, "[]");
        Type type = new TypeToken<List<ReviewCard>>(){}.getType();
        List<ReviewCard> cards = gson.fromJson(json, type);
        return cards != null ? cards : new ArrayList<>();
    }

    private void saveCards(List<ReviewCard> cards) {
        String json = gson.toJson(cards);
        preferences.edit().putString(KEY_CARDS, json).apply();
    }

    public static class ReviewCard {
        private String id;
        private String question;  // Ahora en QUECHUA
        private String answer;    // Ahora en ESPAÑOL
        private String difficulty;
        private String category;
        private int interval;
        private double easeFactor;
        private long nextReview;

        public ReviewCard(String id, String question, String answer, String difficulty,
                          String category, int interval, double easeFactor, long nextReview) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.difficulty = difficulty;
            this.category = category;
            this.interval = interval;
            this.easeFactor = easeFactor;
            this.nextReview = nextReview;
        }

        // Getters y Setters
        public String getId() { return id; }
        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
        public String getDifficulty() { return difficulty; }
        public String getCategory() { return category; }
        public int getInterval() { return interval; }
        public double getEaseFactor() { return easeFactor; }
        public long getNextReview() { return nextReview; }

        public void setInterval(int interval) { this.interval = interval; }
        public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }
        public void setNextReview(long nextReview) { this.nextReview = nextReview; }
    }
}