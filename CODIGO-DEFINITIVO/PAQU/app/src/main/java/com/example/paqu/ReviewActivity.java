package com.example.paqu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paqu.utils.SpacedRepetitionManager;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private TextView textQuestion, textAnswer, textProgress, textWordCount;
    private Button btnShowAnswer, btnEasy, btnMedium, btnHard;
    private LinearLayout layoutAnswerButtons;
    private ProgressBar progressBar;

    private SpacedRepetitionManager reviewManager;
    private List<SpacedRepetitionManager.ReviewCard> currentSessionCards;
    private int currentCardIndex = 0;
    private int cardsReviewed = 0;
    private final int MAX_CARDS_PER_SESSION = 5;
    private boolean isShowingAnswer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        initViews();
        setupReviewManager();
        loadSessionCards();
    }

    private void initViews() {
        textQuestion = findViewById(R.id.textQuestion);
        textAnswer = findViewById(R.id.textAnswer);
        textProgress = findViewById(R.id.textProgress);
        textWordCount = findViewById(R.id.textWordCount);
        btnShowAnswer = findViewById(R.id.btnShowAnswer);
        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);
        layoutAnswerButtons = findViewById(R.id.layoutAnswerButtons);
        progressBar = findViewById(R.id.progressBar);

        btnShowAnswer.setOnClickListener(v -> showAnswer());
        btnEasy.setOnClickListener(v -> rateCard("easy"));
        btnMedium.setOnClickListener(v -> rateCard("medium"));
        btnHard.setOnClickListener(v -> rateCard("hard"));

        textAnswer.setVisibility(View.GONE);
        layoutAnswerButtons.setVisibility(View.GONE);
    }

    private void setupReviewManager() {
        reviewManager = new SpacedRepetitionManager(this);
    }

    private void loadSessionCards() {
        currentSessionCards = reviewManager.getTodaysReviewCards();

        if (currentSessionCards.isEmpty()) {
            showNoCardsMessage();
            return;
        }

        if (currentSessionCards.size() > MAX_CARDS_PER_SESSION) {
            currentSessionCards = currentSessionCards.subList(0, MAX_CARDS_PER_SESSION);
        }

        displayCurrentCard();
        updateProgress();
    }

    private void displayCurrentCard() {
        if (currentCardIndex < currentSessionCards.size()) {
            SpacedRepetitionManager.ReviewCard card = currentSessionCards.get(currentCardIndex);

            // MOSTRAR PRIMERO EN QUECHUA (pregunta)
            textQuestion.setText(card.getQuestion());

            // RESPUESTA EN ESPAÑOL
            textAnswer.setText(card.getAnswer());

            // Mostrar categoría y dificultad
            String dificultad = "";
            switch (card.getDifficulty()) {
                case "easy": dificultad = " (Fácil)"; break;
                case "medium": dificultad = " (Medio)"; break;
                case "hard": dificultad = " (Difícil)"; break;
            }
            textWordCount.setText("Categoría: " + card.getCategory() + dificultad);

            showQuestion();
        }
    }

    private void showAnswer() {
        if (isShowingAnswer) return;

        isShowingAnswer = true;
        textAnswer.setVisibility(View.VISIBLE);
        layoutAnswerButtons.setVisibility(View.VISIBLE);
        btnShowAnswer.setVisibility(View.GONE);
    }

    private void showQuestion() {
        isShowingAnswer = false;
        textAnswer.setVisibility(View.GONE);
        layoutAnswerButtons.setVisibility(View.GONE);
        btnShowAnswer.setVisibility(View.VISIBLE);
    }

    private void rateCard(String difficulty) {
        if (currentCardIndex < currentSessionCards.size()) {
            SpacedRepetitionManager.ReviewCard currentCard = currentSessionCards.get(currentCardIndex);
            reviewManager.rateCard(currentCard.getId(), difficulty);

            cardsReviewed++;
            currentCardIndex++;

            if (currentCardIndex < currentSessionCards.size() && cardsReviewed < MAX_CARDS_PER_SESSION) {
                displayCurrentCard();
                updateProgress();
            } else {
                finishReviewSession();
            }
        }
    }

    private void updateProgress() {
        int totalEnSesion = currentSessionCards.size();
        int progreso = currentCardIndex + 1;

        textProgress.setText(progreso + "/" + totalEnSesion);

        int porcentaje = (int) ((float) progreso / totalEnSesion * 100);
        progressBar.setProgress(porcentaje);
    }

    private void finishReviewSession() {
        int totalCartas = reviewManager.getTotalCards();
        int pendientes = reviewManager.getDueCardsCount();

        // Crear vista personalizada para el diálogo
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 60, 60, 60);

        // Fondo con degradado morado-rosa
        GradientDrawable bgGradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#667eea"), Color.parseColor("#764ba2"), Color.parseColor("#f093fb")}
        );
        bgGradient.setCornerRadius(60f);
        dialogLayout.setBackground(bgGradient);

        // Icono emoji
        TextView iconEmoji = new TextView(this);
        iconEmoji.setText("🎉");
        iconEmoji.setTextSize(56);
        iconEmoji.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.setMargins(0, 0, 0, 40);
        iconEmoji.setLayoutParams(emojiParams);
        dialogLayout.addView(iconEmoji);

        // Título
        TextView title = new TextView(this);
        title.setText("¡Repaso Completado!");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 50);
        title.setLayoutParams(titleParams);
        dialogLayout.addView(title);

        // Tarjeta blanca con estadísticas
        LinearLayout statsCard = new LinearLayout(this);
        statsCard.setOrientation(LinearLayout.VERTICAL);
        statsCard.setPadding(40, 40, 40, 40);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(40f);
        statsCard.setBackground(cardBg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 50);
        statsCard.setLayoutParams(cardParams);

        // Estadísticas
        String[] stats = {
                "✅ Palabras repasadas: " + cardsReviewed,
                "📚 Total en biblioteca: " + totalCartas + " palabras",
                "⏰ Pendientes para hoy: " + pendientes,
                "\n¡Sigue practicando el Quechua!"
        };

        for (int i = 0; i < stats.length; i++) {
            TextView statText = new TextView(this);
            statText.setText(stats[i]);
            statText.setTextSize(i == 3 ? 14 : 16);
            statText.setTextColor(i == 3 ? Color.parseColor("#4A5568") : Color.parseColor("#2D3748"));
            if (i == 3) statText.setTypeface(null, android.graphics.Typeface.ITALIC);
            LinearLayout.LayoutParams statParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            statParams.setMargins(0, 0, 0, i < 2 ? 20 : 0);
            statText.setLayoutParams(statParams);
            statsCard.addView(statText);
        }

        dialogLayout.addView(statsCard);

        // Botón continuar
        Button btnContinue = new Button(this);
        btnContinue.setText("Continuar");
        btnContinue.setTextSize(18);
        btnContinue.setTextColor(Color.WHITE);
        btnContinue.setTypeface(null, android.graphics.Typeface.BOLD);
        btnContinue.setPadding(0, 40, 0, 40);
        GradientDrawable btnGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#8E2DE2"), Color.parseColor("#4A00E0")}
        );
        btnGradient.setCornerRadius(30f);
        btnContinue.setBackground(btnGradient);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnContinue.setLayoutParams(btnParams);
        dialogLayout.addView(btnContinue);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void showNoCardsMessage() {
        // Crear vista personalizada para el diálogo
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 60, 60, 60);

        // Fondo con degradado verde
        GradientDrawable bgGradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#11998e"), Color.parseColor("#38ef7d"), Color.parseColor("#06beb6")}
        );
        bgGradient.setCornerRadius(60f);
        dialogLayout.setBackground(bgGradient);

        // Icono emoji
        TextView iconEmoji = new TextView(this);
        iconEmoji.setText("✅");
        iconEmoji.setTextSize(56);
        iconEmoji.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.setMargins(0, 0, 0, 40);
        iconEmoji.setLayoutParams(emojiParams);
        dialogLayout.addView(iconEmoji);

        // Título
        TextView title = new TextView(this);
        title.setText("Al día");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 50);
        title.setLayoutParams(titleParams);
        dialogLayout.addView(title);

        // Tarjeta blanca con mensaje
        LinearLayout messageCard = new LinearLayout(this);
        messageCard.setOrientation(LinearLayout.VERTICAL);
        messageCard.setPadding(40, 40, 40, 40);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(40f);
        messageCard.setBackground(cardBg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 50);
        messageCard.setLayoutParams(cardParams);

        TextView message1 = new TextView(this);
        message1.setText("No hay palabras pendientes para repasar hoy.");
        message1.setTextSize(16);
        message1.setTextColor(Color.parseColor("#2D3748"));
        LinearLayout.LayoutParams msg1Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msg1Params.setMargins(0, 0, 0, 30);
        message1.setLayoutParams(msg1Params);
        messageCard.addView(message1);

        TextView message2 = new TextView(this);
        message2.setText("¡Excelente trabajo! Vuelve mañana para continuar aprendiendo Quechua.");
        message2.setTextSize(14);
        message2.setTextColor(Color.parseColor("#4A5568"));
        message2.setTypeface(null, android.graphics.Typeface.ITALIC);
        messageCard.addView(message2);

        dialogLayout.addView(messageCard);

        // Botón OK
        Button btnOk = new Button(this);
        btnOk.setText("OK");
        btnOk.setTextSize(18);
        btnOk.setTextColor(Color.WHITE);
        btnOk.setTypeface(null, android.graphics.Typeface.BOLD);
        btnOk.setPadding(0, 40, 0, 40);
        GradientDrawable btnGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#56ab2f"), Color.parseColor("#a8e063")}
        );
        btnGradient.setCornerRadius(30f);
        btnOk.setBackground(btnGradient);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnOk.setLayoutParams(btnParams);
        dialogLayout.addView(btnOk);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        // Crear vista personalizada para el diálogo
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 60, 60, 60);

        // Fondo con degradado naranja-amarillo
        GradientDrawable bgGradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#f46b45"), Color.parseColor("#eea849"), Color.parseColor("#ffa751")}
        );
        bgGradient.setCornerRadius(60f);
        dialogLayout.setBackground(bgGradient);

        // Icono emoji
        TextView iconEmoji = new TextView(this);
        iconEmoji.setText("⚠️");
        iconEmoji.setTextSize(56);
        iconEmoji.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.setMargins(0, 0, 0, 40);
        iconEmoji.setLayoutParams(emojiParams);
        dialogLayout.addView(iconEmoji);

        // Título
        TextView title = new TextView(this);
        title.setText("Salir del repaso");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 50);
        title.setLayoutParams(titleParams);
        dialogLayout.addView(title);

        // Tarjeta blanca con mensaje
        LinearLayout messageCard = new LinearLayout(this);
        messageCard.setOrientation(LinearLayout.VERTICAL);
        messageCard.setPadding(40, 40, 40, 40);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(40f);
        messageCard.setBackground(cardBg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 50);
        messageCard.setLayoutParams(cardParams);

        TextView message = new TextView(this);
        message.setText("¿Estás seguro de que quieres salir? Tu progreso se guardará automáticamente.");
        message.setTextSize(16);
        message.setTextColor(Color.parseColor("#2D3748"));
        messageCard.addView(message);

        dialogLayout.addView(messageCard);

        // Contenedor de botones
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonsLayout.setLayoutParams(buttonsParams);

        // Botón No
        Button btnNo = new Button(this);
        btnNo.setText("No");
        btnNo.setTextSize(18);
        btnNo.setTextColor(Color.WHITE);
        btnNo.setTypeface(null, android.graphics.Typeface.BOLD);
        btnNo.setPadding(0, 40, 0, 40);
        GradientDrawable btnNoGradient = new GradientDrawable();
        btnNoGradient.setColor(Color.parseColor("#718096"));
        btnNoGradient.setCornerRadius(30f);
        btnNo.setBackground(btnNoGradient);
        LinearLayout.LayoutParams btnNoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        btnNoParams.setMargins(0, 0, 20, 0);
        btnNo.setLayoutParams(btnNoParams);
        buttonsLayout.addView(btnNo);

        // Botón Sí
        Button btnYes = new Button(this);
        btnYes.setText("Sí");
        btnYes.setTextSize(18);
        btnYes.setTextColor(Color.WHITE);
        btnYes.setTypeface(null, android.graphics.Typeface.BOLD);
        btnYes.setPadding(0, 40, 0, 40);
        GradientDrawable btnYesGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#eb3349"), Color.parseColor("#f45c43")}
        );
        btnYesGradient.setCornerRadius(30f);
        btnYes.setBackground(btnYesGradient);
        LinearLayout.LayoutParams btnYesParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        btnYesParams.setMargins(20, 0, 0, 0);
        btnYes.setLayoutParams(btnYesParams);
        buttonsLayout.addView(btnYes);

        dialogLayout.addView(buttonsLayout);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}