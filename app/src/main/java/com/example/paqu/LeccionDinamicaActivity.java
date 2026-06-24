package com.example.paqu;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.BounceInterpolator;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.paqu.utils.QuechuaTTSManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

/**
 * Ejecuta los ejercicios dinámicos de una lección creada por el docente.
 *
 * Flujo:
 *   1. Carga ejercicios desde Firebase (lessons/{id}/content/ejercicios)
 *   2. Muestra cada ejercicio según su tipo (SELECCION_MULTIPLE / CONSTRUCCION_FRASE / ESCRITURA_LIBRE)
 *   3. Al completar todos → guarda en user_lessons y va a LeccionAcabadaActivity
 *
 * Audio: usa QuechuaTTSManager para reproducir la palabra/frase del campo "palabraAudio"
 */
public class LeccionDinamicaActivity extends AppCompatActivity {

    private static final String TAG = "LeccionDinamica";

    // Tipos de ejercicio (mismo enum que CrearLeccionActivity)
    private static final String TIPO_SELECCION = "SELECCION_MULTIPLE";
    private static final String TIPO_FRASE     = "CONSTRUCCION_FRASE";
    private static final String TIPO_ESCRITURA = "ESCRITURA_LIBRE";

    // Datos de la lección
    private String        leccionId;
    private String        leccionTitulo;
    private List<Map<String, Object>> ejercicios = new ArrayList<>();
    private int           indiceActual   = 0;
    private int           expAcumulado   = 0;
    private long          tiempoInicio;

    // Vidas
    private long   vidasActuales = 5;
    private String userId;

    // Vistas del contenedor principal
    private ProgressBar progressBar;
    private TextView    tvTituloLeccion;
    private TextView    tvNumeroPregunta;
    private TextView    tvVidasCount;
    private ImageView   btnBackToMenu;

    // Contenedor dinámico (se limpia y rellena por tipo)
    private LinearLayout  contenedorEjercicio;

    // TTS
    private QuechuaTTSManager ttsManager;
    private AudioManager      audioManager;

    // Firebase
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leccion_dinamica);

        userId      = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef       = FirebaseDatabase.getInstance().getReference();
        ttsManager  = new QuechuaTTSManager(this);
        audioManager = AudioManager.getInstance(this);

        leccionId     = getIntent().getStringExtra("LECCION_ID");
        leccionTitulo = getIntent().getStringExtra("LECCION_TITLE");
        vidasActuales = getIntent().getLongExtra("vidas", 5);

        if (leccionId == null) { finish(); return; }

        initViews();
        cargarEjercicios();
    }

    // ──────────────────────────────────────────
    // INICIALIZAR VISTAS
    // ──────────────────────────────────────────
    private void initViews() {
        progressBar         = findViewById(R.id.progressBarLeccion);
        tvTituloLeccion     = findViewById(R.id.tvTituloLeccionDin);
        tvNumeroPregunta    = findViewById(R.id.tvNumeroPregunta);
        tvVidasCount        = findViewById(R.id.livesCount);
        btnBackToMenu       = findViewById(R.id.btnBackToMenu);
        contenedorEjercicio = findViewById(R.id.contenedorEjercicio);

        tvTituloLeccion.setText(leccionTitulo != null ? leccionTitulo : "Lección");
        tvVidasCount.setText(String.valueOf(vidasActuales));

        btnBackToMenu.setOnClickListener(v -> mostrarDialogoSalir());
    }

    // ──────────────────────────────────────────
    // CARGAR EJERCICIOS DESDE FIREBASE
    // ──────────────────────────────────────────
    private void cargarEjercicios() {
        mostrarCargando(true);

        dbRef.child("lessons").child(leccionId).child("content/ejercicios")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ejercicios.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Map<String, Object> ej = new HashMap<>();
                            ej.put("tipo",             getStr(ds, "tipo",             TIPO_ESCRITURA));
                            ej.put("pregunta",         getStr(ds, "pregunta",         ""));
                            ej.put("respuestaCorrecta", getStr(ds, "respuestaCorrecta", ""));
                            ej.put("palabraAudio",     getStr(ds, "palabraAudio",     ""));

                            // Opciones (lista)
                            List<String> opciones = new ArrayList<>();
                            for (DataSnapshot op : ds.child("opciones").getChildren()) {
                                String val = op.getValue(String.class);
                                if (val != null) opciones.add(val);
                            }
                            ej.put("opciones", opciones);
                            ejercicios.add(ej);
                        }

                        mostrarCargando(false);

                        if (ejercicios.isEmpty()) {
                            Toast.makeText(LeccionDinamicaActivity.this,
                                    "Esta lección no tiene ejercicios aún", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        tiempoInicio = System.currentTimeMillis();
                        mostrarEjercicio(0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mostrarCargando(false);
                        Toast.makeText(LeccionDinamicaActivity.this,
                                "Error cargando ejercicios", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    // ──────────────────────────────────────────
    // MOSTRAR EJERCICIO SEGÚN ÍNDICE
    // ──────────────────────────────────────────
    private void mostrarEjercicio(int index) {
        if (index >= ejercicios.size()) {
            finalizarLeccion();
            return;
        }

        indiceActual = index;
        Map<String, Object> ej = ejercicios.get(index);

        // Actualizar barra de progreso
        int prog = ((index) * 100) / ejercicios.size();
        progressBar.setProgress(prog);
        tvNumeroPregunta.setText((index + 1) + " / " + ejercicios.size());

        // Limpiar contenedor
        contenedorEjercicio.removeAllViews();

        String tipo = (String) ej.get("tipo");

        // Animación de entrada
        contenedorEjercicio.setAlpha(0f);
        contenedorEjercicio.animate().alpha(1f).setDuration(300).start();

        switch (tipo != null ? tipo : "") {
            case TIPO_SELECCION: mostrarSeleccionMultiple(ej); break;
            case TIPO_FRASE:     mostrarConstruccionFrase(ej); break;
            case TIPO_ESCRITURA: mostrarEscrituraLibre(ej);    break;
            default:             mostrarEscrituraLibre(ej);    break;
        }
    }

    // ──────────────────────────────────────────
    // TIPO 1: SELECCIÓN MÚLTIPLE
    // ──────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void mostrarSeleccionMultiple(Map<String, Object> ej) {
        String pregunta     = (String) ej.get("pregunta");
        String respuesta    = (String) ej.get("respuestaCorrecta");
        String palabraAudio = (String) ej.get("palabraAudio");
        List<String> opciones = (List<String>) ej.get("opciones");

        // 1. Pregunta + botón audio
        agregarPreguntaConAudio(pregunta, palabraAudio);

        // 2. Opciones
        if (opciones == null || opciones.isEmpty()) return;

        final String[] seleccionada = {null};

        // Inflar botón "Comprobar"
        Button btnCheck = crearBtnComprobar();
        List<LinearLayout> opcionLayouts = new ArrayList<>();

        for (String opcion : opciones) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);
            card.setPadding(32, 24, 32, 24);
            card.setBackground(getDrawable(R.drawable.option_background));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            TextView tvOpcion = new TextView(this);
            tvOpcion.setText(opcion);
            tvOpcion.setTextSize(17f);
            tvOpcion.setTextColor(Color.parseColor("#1E1E24"));
            card.addView(tvOpcion);

            card.setOnClickListener(v -> {
                // Deseleccionar todos
                for (LinearLayout ol : opcionLayouts) ol.setSelected(false);
                card.setSelected(true);
                seleccionada[0] = opcion;

                btnCheck.setEnabled(true);
                btnCheck.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#1E1E24")));
                btnCheck.setTextColor(Color.WHITE);

                audioManager.reproducirClic();
            });

            opcionLayouts.add(card);
            contenedorEjercicio.addView(card);
        }

        contenedorEjercicio.addView(btnCheck);

        btnCheck.setOnClickListener(v -> {
            if (seleccionada[0] == null) return;
            if (normalizar(seleccionada[0]).equals(normalizar(respuesta))) {
                audioManager.reproducirExito();
                expAcumulado += 10;
                siguienteEjercicio();
            } else {
                audioManager.reproducirError();
                restarVida();
            }
        });
    }

    // ──────────────────────────────────────────
    // TIPO 2: CONSTRUCCIÓN DE FRASE
    // ──────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void mostrarConstruccionFrase(Map<String, Object> ej) {
        String pregunta     = (String) ej.get("pregunta");
        String respuesta    = (String) ej.get("respuestaCorrecta");
        String palabraAudio = (String) ej.get("palabraAudio");
        List<String> opciones = (List<String>) ej.get("opciones");

        agregarPreguntaConAudio(pregunta, palabraAudio);

        // Zona de frase construida
        FlexboxLayout zonaFrase = new FlexboxLayout(this);
        zonaFrase.setMinimumHeight(80);
        zonaFrase.setBackground(getDrawable(R.drawable.bg_zona_frase));
        LinearLayout.LayoutParams zpLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        zpLp.setMargins(0, 0, 0, 16);
        zonaFrase.setLayoutParams(zpLp);
        contenedorEjercicio.addView(zonaFrase);

        Button btnCheck = crearBtnComprobar();
        List<String> palabrasElegidas = new ArrayList<>();
        Map<String, LinearLayout> mapaLayouts = new HashMap<>();

        if (opciones == null || opciones.isEmpty()) return;
        List<String> opcionesMezcladas = new ArrayList<>(opciones);
        Collections.shuffle(opcionesMezcladas);

        LinearLayout contenedorOpciones = new LinearLayout(this);
        contenedorOpciones.setOrientation(LinearLayout.HORIZONTAL);
        contenedorOpciones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) contenedorOpciones.getLayoutParams())
                .setMargins(0, 0, 0, 16);

        for (String palabra : opcionesMezcladas) {
            LinearLayout chip = crearChipPalabra(palabra);
            mapaLayouts.put(palabra, chip);

            chip.setOnClickListener(v -> {
                if (palabrasElegidas.contains(palabra)) return;
                palabrasElegidas.add(palabra);
                chip.setAlpha(0.3f);
                agregarPalabraAZona(palabra, zonaFrase, palabrasElegidas, chip, mapaLayouts);

                btnCheck.setEnabled(true);
                btnCheck.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#1E1E24")));
                btnCheck.setTextColor(Color.WHITE);
            });

            contenedorOpciones.addView(chip);
        }

        contenedorEjercicio.addView(contenedorOpciones);
        contenedorEjercicio.addView(btnCheck);

        btnCheck.setOnClickListener(v -> {
            String fraseUsuario = TextUtils.join(" ", palabrasElegidas).trim();
            if (normalizar(fraseUsuario).equals(normalizar(respuesta))) {
                audioManager.reproducirExito();
                expAcumulado += 10;
                siguienteEjercicio();
            } else {
                audioManager.reproducirError();
                restarVida();
            }
        });
    }

    private void agregarPalabraAZona(String palabra, FlexboxLayout zona,
                                     List<String> lista, LinearLayout chipOriginal,
                                     Map<String, LinearLayout> mapaLayouts) {
        TextView tv = new TextView(this);
        tv.setText(palabra);
        tv.setTextSize(15f);
        tv.setPadding(20, 10, 20, 10);
        tv.setBackground(getDrawable(R.drawable.option_background));
        tv.setTextColor(Color.parseColor("#1E1E24"));

        com.google.android.flexbox.FlexboxLayout.LayoutParams fp =
                new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fp.setMargins(6, 6, 6, 6);
        tv.setLayoutParams(fp);

        tv.setOnClickListener(v -> {
            lista.remove(palabra);
            zona.removeView(tv);
            if (mapaLayouts.containsKey(palabra))
                mapaLayouts.get(palabra).setAlpha(1f);
        });

        zona.addView(tv);
    }

    private LinearLayout crearChipPalabra(String palabra) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setPadding(20, 12, 20, 12);
        chip.setBackground(getDrawable(R.drawable.option_background));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 10, 0);
        chip.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(palabra);
        tv.setTextSize(15f);
        tv.setTextColor(Color.parseColor("#1E1E24"));
        chip.addView(tv);
        return chip;
    }

    // ──────────────────────────────────────────
    // TIPO 3: ESCRITURA LIBRE
    // ──────────────────────────────────────────
    private void mostrarEscrituraLibre(Map<String, Object> ej) {
        String pregunta     = (String) ej.get("pregunta");
        String respuesta    = (String) ej.get("respuestaCorrecta");
        String palabraAudio = (String) ej.get("palabraAudio");

        agregarPreguntaConAudio(pregunta, palabraAudio);

        // Campo de texto
        EditText et = new EditText(this);
        et.setHint("Escribe en Quechua...");
        et.setTextSize(17f);
        et.setBackground(getDrawable(R.drawable.bg_edittext_leccion));
        et.setPadding(20, 16, 20, 16);
        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etLp.setMargins(0, 0, 0, 16);
        et.setLayoutParams(etLp);
        contenedorEjercicio.addView(et);

        Button btnCheck = crearBtnComprobar();
        contenedorEjercicio.addView(btnCheck);

        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                boolean empty = s.toString().trim().isEmpty();
                btnCheck.setEnabled(!empty);
                btnCheck.setBackgroundTintList(ColorStateList.valueOf(
                        empty ? Color.parseColor("#D6C7EB") : Color.parseColor("#1E1E24")));
                btnCheck.setTextColor(
                        empty ? Color.parseColor("#A594BF") : Color.WHITE);
            }
        });

        btnCheck.setOnClickListener(v -> {
            String input = et.getText().toString().trim();
            if (normalizar(input).equals(normalizar(respuesta))) {
                audioManager.reproducirExito();
                expAcumulado += 10;
                siguienteEjercicio();
            } else {
                audioManager.reproducirError();
                restarVida();
            }
        });
    }

    // ──────────────────────────────────────────
    // HELPERS DE UI
    // ──────────────────────────────────────────
    private void agregarPreguntaConAudio(String pregunta, String palabraAudio) {
        // Texto de la pregunta
        TextView tvPregunta = new TextView(this);
        tvPregunta.setText(pregunta);
        tvPregunta.setTextSize(18f);
        tvPregunta.setTextColor(Color.parseColor("#1E1E24"));
        tvPregunta.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 24);
        tvPregunta.setLayoutParams(lp);
        contenedorEjercicio.addView(tvPregunta);

        // Botón audio (solo si hay texto de audio)
        if (palabraAudio != null && !palabraAudio.isEmpty()) {
            LinearLayout btnAudio = new LinearLayout(this);
            btnAudio.setOrientation(LinearLayout.HORIZONTAL);
            btnAudio.setGravity(android.view.Gravity.CENTER);
            btnAudio.setPadding(40, 20, 40, 20);
            btnAudio.setBackground(getDrawable(R.drawable.bg_btn_audio));

            LinearLayout.LayoutParams audioLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            audioLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            audioLp.setMargins(0, 0, 0, 24);
            btnAudio.setLayoutParams(audioLp);

            ImageView icono = new ImageView(this);
            icono.setImageResource(R.drawable.ic_volume_up);
            icono.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
            icono.setColorFilter(Color.parseColor("#FF6F00"));
            btnAudio.addView(icono);

            TextView tvAudioLabel = new TextView(this);
            tvAudioLabel.setText("  Escuchar");
            tvAudioLabel.setTextColor(Color.parseColor("#FF6F00"));
            tvAudioLabel.setTextSize(14f);
            btnAudio.addView(tvAudioLabel);

            final String audioTexto = palabraAudio;
            btnAudio.setOnClickListener(v -> {
                icono.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                        .withEndAction(() -> icono.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();
                ttsManager.reproducir(audioTexto, null);
            });

            contenedorEjercicio.addView(btnAudio);
        }
    }

    private Button crearBtnComprobar() {
        Button btn = new Button(this);
        btn.setText("Comprobar");
        btn.setTextColor(Color.parseColor("#A594BF"));
        btn.setTextSize(16f);
        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D6C7EB")));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 140);
        lp.setMargins(0, 16, 0, 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    private void mostrarCargando(boolean show) {
        View loader = findViewById(R.id.progressBarCargaLeccion);
        if (loader != null) loader.setVisibility(show ? View.VISIBLE : View.GONE);
        if (contenedorEjercicio != null)
            contenedorEjercicio.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ──────────────────────────────────────────
    // LÓGICA DE VIDAS
    // ──────────────────────────────────────────
    private void restarVida() {
        vidasActuales = Math.max(vidasActuales - 1, 0);
        guardarVidas();
        tvVidasCount.setText(String.valueOf(vidasActuales));

        if (vidasActuales <= 0) {
            Toast.makeText(this, "💔 Sin vidas. Vuelve mañana.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, homeActivity.class));
            finish();
        } else {
            Toast.makeText(this,
                    "❌ Incorrecto. Te quedan " + vidasActuales + " vidas.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarVidas() {
        getSharedPreferences("game_data", MODE_PRIVATE)
                .edit().putLong("vidas", vidasActuales).apply();
    }

    // ──────────────────────────────────────────
    // SIGUIENTE EJERCICIO / FINALIZAR
    // ──────────────────────────────────────────
    private void siguienteEjercicio() {
        int siguiente = indiceActual + 1;
        if (siguiente < ejercicios.size()) {
            mostrarEjercicio(siguiente);
        } else {
            finalizarLeccion();
        }
    }

    private void finalizarLeccion() {
        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
        guardarProgresoEnFirebase(tiempoTotal);

        Intent intent = new Intent(this, LeccionAcabadaActivity.class);
        intent.putExtra("exp",     expAcumulado);
        intent.putExtra("tiempo",  tiempoTotal);
        intent.putExtra("nivel",   1);
        intent.putExtra("vidas",   vidasActuales);
        startActivity(intent);
        finish();
    }

    /**
     * Guarda en user_lessons/{userId}_{leccionId}
     * que el estudiante completó esta lección.
     */
    private void guardarProgresoEnFirebase(long tiempo) {
        String userLessonId = userId + "_" + leccionId;

        Map<String, Object> data = new HashMap<>();
        data.put("userId",      userId);
        data.put("lessonId",    leccionId);
        data.put("completed",   true);
        data.put("score",       expAcumulado);
        data.put("tiempo",      tiempo);
        data.put("completadoEn", System.currentTimeMillis());

        dbRef.child("user_lessons").child(userLessonId).setValue(data)
                .addOnSuccessListener(a -> Log.d(TAG, "Progreso guardado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error guardando progreso: " + e.getMessage()));

        // Actualizar stats generales del usuario
        dbRef.child("users").child(userId).child("progress")
                .child("totalXP").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        Long xp = snap.getValue(Long.class);
                        long nuevoXp = (xp != null ? xp : 0) + expAcumulado;
                        dbRef.child("users").child(userId)
                                .child("progress/totalXP").setValue(nuevoXp);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ──────────────────────────────────────────
    // DIÁLOGO SALIR
    // ──────────────────────────────────────────
    private void mostrarDialogoSalir() {
        new AlertDialog.Builder(this)
                .setTitle("¿Quieres salir? 😟")
                .setMessage("Perderás el progreso de esta lección.")
                .setPositiveButton("Sí, salir", (d, w) -> {
                    guardarVidas();
                    Intent i = new Intent(this, homeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onBackPressed() { mostrarDialogoSalir(); }

    // ──────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────
    private String normalizar(String s) {
        return s.trim().toLowerCase()
                .replace("'", "'").replace("'", "'")
                .replace("`", "'").replace("´", "'").replace("ʻ", "'");
    }

    private String getStr(DataSnapshot ds, String key, String def) {
        String v = ds.child(key).getValue(String.class);
        return v != null ? v : def;
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarVidas();
        if (ttsManager != null) ttsManager.detener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) ttsManager.detener();
    }
}