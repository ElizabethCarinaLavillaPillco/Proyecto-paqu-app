package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.utils.FloatingChatManager;
import com.example.paqu.utils.QuechuaTTSManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.example.paqu.utils.StreakManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Home del ESTUDIANTE — versión actualizada.
 *
 * Cambios respecto a la versión anterior:
 *  - Las 6 lecciones hardcodeadas se REEMPLAZAN por lecciones dinámicas de Firebase.
 *  - Cada lección tiene un LeccionDinamicaCard que muestra título, categoría, nivel,
 *    número de ejercicios, EXP y estado (completada / bloqueada / disponible).
 *  - El progreso general se calcula sobre las lecciones de Firebase.
 *  - Al tocar una lección disponible abre LeccionDinamicaActivity (nueva).
 *  - Las burbujas de repaso y curiosidades se mantienen.
 *  - El TTS Quechua está integrado vía QuechuaTTSManager.
 */
public class homeActivity extends BaseActivity {

    private static final String TAG = "homeActivity";

    // ── Header stats ──
    private TextView streakDays, diamondsCount, livesCount;

    // ── Sección sticky ──
    private CardView       stickySection;
    private LinearLayout   sectionContent;
    private TextView       tvSectionNumber, tvSectionTitle, tvSectionDescription;
    private ProgressBar    progressBarGeneral;
    private TextView       tvPorcentajeCompletado;
    private int            stickySectionTop;

    // ── RecyclerView de lecciones dinámicas ──
    private RecyclerView       rvLecciones;
    private LeccionesAdapter   leccionesAdapter;
    private List<LeccionCard>  listaLecciones;

    // ── Burbujas flotantes ──
    private CardView draggableBubble, curiositiesBubble;

    // ── Firebase ──
    private DatabaseReference dbRef;
    private String            userId;

    // ── Managers ──
    private StreakManager     streakManager;
    private QuechuaTTSManager ttsManager;

    // ── Mapa / variantes ──
    private CardView  cardMapaVariantes;
    private ImageView ivMapaQuechua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, loginFormActivity.class));
            finish();
            return;
        }
        userId  = user.getUid();
        dbRef   = FirebaseDatabase.getInstance().getReference();

        // Managers
        streakManager = new StreakManager();
        ttsManager    = new QuechuaTTSManager(this);

        initViews();
        setupBottomNav();
        setupStickyHeader();
        setupBurbujas();
        setupMapaButton();
        aplicarFuentesAutomaticas();

        // Inicializar vidas si es primera vez
        SharedPreferences prefs = getSharedPreferences("game_data", MODE_PRIVATE);
        if (!prefs.contains("vidas")) {
            prefs.edit().putLong("vidas", 5).apply();
        }

        // Cargar datos
        updateStreakAndData();
        cargarLeccionesFirebase();

        mostrarToastBienvenida();
    }

    // ══════════════════════════════════════════════════════════════
    // INICIALIZAR VISTAS
    // ══════════════════════════════════════════════════════════════
    private void initViews() {
        streakDays   = findViewById(R.id.streakDays);
        diamondsCount = findViewById(R.id.diamondsCount);
        livesCount   = findViewById(R.id.livesCount);

        tvSectionNumber      = findViewById(R.id.tvSectionNumber);
        tvSectionTitle       = findViewById(R.id.tvSectionTitle);
        tvSectionDescription = findViewById(R.id.tvSectionDescription);
        progressBarGeneral   = findViewById(R.id.sectionProgressBar);
        tvPorcentajeCompletado = findViewById(R.id.tvPorcentajeCompletado);

        stickySection  = findViewById(R.id.stickySection);
        sectionContent = findViewById(R.id.sectionContent);

        // RecyclerView — reemplaza los level1Card..level6Card del layout anterior
        rvLecciones    = findViewById(R.id.rvLeccionesDinamicas);
        listaLecciones = new ArrayList<>();
        leccionesAdapter = new LeccionesAdapter(listaLecciones, this);
        rvLecciones.setLayoutManager(new LinearLayoutManager(this));
        rvLecciones.setAdapter(leccionesAdapter);
        rvLecciones.setNestedScrollingEnabled(false);

        draggableBubble  = findViewById(R.id.draggableBubble);
        curiositiesBubble = findViewById(R.id.curiositiesBubble);

        cardMapaVariantes = findViewById(R.id.cardMapaVariantes);
        ivMapaQuechua     = findViewById(R.id.ivMapaQuechua);
    }

    // ══════════════════════════════════════════════════════════════
    // CARGAR LECCIONES DESDE FIREBASE  ← NÚCLEO DEL CAMBIO
    // ══════════════════════════════════════════════════════════════
    private void cargarLeccionesFirebase() {
        // Mostrar skeleton/loader
        mostrarLoader(true);

        dbRef.child("lessons")
                .orderByChild("lessonInfo/activa")
                .equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaLecciones.clear();

                        // Obtener lecciones completadas del estudiante
                        obtenerProgreso(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error cargando lecciones: " + error.getMessage());
                        mostrarLoader(false);
                        mostrarErrorLecciones();
                    }
                });
    }

    /**
     * Primero obtiene el progreso del estudiante (lecciones completadas),
     * luego cruza con las lecciones activas de Firebase.
     */
    private void obtenerProgreso(DataSnapshot leccionesSnapshot) {
        dbRef.child("user_lessons")
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot progresoSnapshot) {
                        // Construir set de lessonIds completadas
                        java.util.Set<String> completadas = new java.util.HashSet<>();
                        for (DataSnapshot up : progresoSnapshot.getChildren()) {
                            Boolean comp = up.child("completed").getValue(Boolean.class);
                            if (Boolean.TRUE.equals(comp)) {
                                String lid = up.child("lessonId").getValue(String.class);
                                if (lid != null) completadas.add(lid);
                            }
                        }

                        // Parsear lecciones activas
                        List<LeccionCard> nuevas = new ArrayList<>();
                        for (DataSnapshot ls : leccionesSnapshot.getChildren()) {
                            try {
                                LeccionCard card = parsearLeccion(ls, completadas);
                                if (card != null) nuevas.add(card);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando lección: " + e.getMessage());
                            }
                        }

                        // Ordenar: completadas → disponibles → bloqueadas
                        // y dentro de cada grupo por fecha de creación
                        nuevas.sort((a, b) -> {
                            int prioA = prioridadOrden(a);
                            int prioB = prioridadOrden(b);
                            if (prioA != prioB) return Integer.compare(prioA, prioB);
                            return Long.compare(a.createdAt, b.createdAt);
                        });

                        // Aplicar lógica de bloqueo secuencial:
                        // la primera no completada está disponible, las siguientes bloqueadas
                        boolean encontroPrimera = false;
                        for (LeccionCard c : nuevas) {
                            if (!c.completada && !encontroPrimera) {
                                c.disponible   = true;
                                encontroPrimera = true;
                            } else if (!c.completada) {
                                c.disponible = false; // bloqueada
                            } else {
                                c.disponible = true;  // completada = siempre accesible
                            }
                        }

                        runOnUiThread(() -> {
                            listaLecciones.clear();
                            listaLecciones.addAll(nuevas);
                            leccionesAdapter.notifyDataSetChanged();
                            actualizarProgresoGeneral(nuevas, completadas.size());
                            mostrarLoader(false);

                            if (nuevas.isEmpty()) mostrarEstadoVacio();
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error progreso: " + error.getMessage());
                        mostrarLoader(false);
                    }
                });
    }

    private LeccionCard parsearLeccion(DataSnapshot ls,
                                       java.util.Set<String> completadas) {
        DataSnapshot info = ls.child("lessonInfo");
        LeccionCard card = new LeccionCard();
        card.id          = ls.getKey();
        card.titulo      = getStr(info, "title",       "Sin título");
        card.descripcion = getStr(info, "description", "");
        card.nivel       = getStr(info, "nivel",       "Básico");
        card.categoria   = getStr(info, "categoria",   "Otra");

        Long ts = info.child("createdAt").getValue(Long.class);
        card.createdAt = ts != null ? ts : 0L;

        card.numEjercicios = (int) ls.child("content/ejercicios").getChildrenCount();

        Long exp = ls.child("rewards/exp").getValue(Long.class);
        card.exp = exp != null ? exp.intValue() : 30;

        card.completada = completadas.contains(card.id);
        card.disponible = false; // se calcula después

        if (card.id == null || card.titulo.isEmpty()) return null;
        return card;
    }

    private int prioridadOrden(LeccionCard c) {
        if (c.completada) return 0;
        if (c.disponible) return 1;
        return 2;
    }

    // ══════════════════════════════════════════════════════════════
    // PROGRESO GENERAL (barra + texto de sección sticky)
    // ══════════════════════════════════════════════════════════════
    private void actualizarProgresoGeneral(List<LeccionCard> todas, int numCompletadas) {
        int total = todas.size();
        if (total == 0) {
            tvSectionNumber.setText("Sin lecciones");
            tvSectionTitle.setText("El docente aún no publicó lecciones");
            tvSectionDescription.setText("Vuelve pronto 📚");
            progressBarGeneral.setProgress(0);
            tvPorcentajeCompletado.setText("0% completado");
            return;
        }

        int porcentaje = (numCompletadas * 100) / total;

        if (numCompletadas >= total) {
            tvSectionNumber.setText("🏆 COMPLETADO");
            tvSectionTitle.setText("¡Felicitaciones!");
            tvSectionDescription.setText("Has completado todas las lecciones disponibles");
        } else {
            // Mostrar la primera lección disponible en la sticky
            LeccionCard proxima = null;
            for (LeccionCard c : todas) {
                if (!c.completada && c.disponible) { proxima = c; break; }
            }
            if (proxima != null) {
                tvSectionNumber.setText("Siguiente");
                tvSectionTitle.setText(proxima.titulo);
                tvSectionDescription.setText(
                        proxima.categoria + " · " + proxima.nivel
                                + " · " + proxima.numEjercicios + " ejercicios"
                );
            }
        }

        progressBarGeneral.setMax(100);
        progressBarGeneral.setProgress(porcentaje);
        tvPorcentajeCompletado.setText(porcentaje + "% completado");
    }

    // ══════════════════════════════════════════════════════════════
    // ESTADOS DE UI
    // ══════════════════════════════════════════════════════════════
    private void mostrarLoader(boolean show) {
        View loader = findViewById(R.id.progressBarLecciones);
        if (loader != null) loader.setVisibility(show ? View.VISIBLE : View.GONE);
        if (rvLecciones != null) rvLecciones.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void mostrarEstadoVacio() {
        View vacio = findViewById(R.id.layoutSinLecciones);
        if (vacio != null) {
            vacio.setVisibility(View.VISIBLE);
            rvLecciones.setVisibility(View.GONE);
        }
    }

    private void mostrarErrorLecciones() {
        Toast.makeText(this,
                "No se pudieron cargar las lecciones. Verifica tu conexión.",
                Toast.LENGTH_LONG).show();
    }

    // ══════════════════════════════════════════════════════════════
    // ABRIR LECCIÓN DINÁMICA
    // ══════════════════════════════════════════════════════════════
    public void abrirLeccion(LeccionCard leccion) {
        if (!leccion.disponible && !leccion.completada) {
            Toast.makeText(this,
                    "🔒 Completa la lección anterior primero",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("game_data", MODE_PRIVATE);
        long vidas = prefs.getLong("vidas", 5);

        if (vidas <= 0) {
            mostrarDialogoSinVidas();
            return;
        }

        Intent intent = new Intent(this, LeccionDinamicaActivity.class);
        intent.putExtra("LECCION_ID",    leccion.id);
        intent.putExtra("LECCION_TITLE", leccion.titulo);
        intent.putExtra("vidas",         vidas);
        startActivity(intent);
    }

    private void mostrarDialogoSinVidas() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("💔 Sin vidas")
                .setMessage("Espera 24 horas para que se recarguen tus vidas, o continúa mañana.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    // ══════════════════════════════════════════════════════════════
    // STREAK Y DATOS DE USUARIO
    // ══════════════════════════════════════════════════════════════
    private void updateStreakAndData() {
        streakManager.updateUserStreak(userId, new StreakManager.StreakUpdateCallback() {
            @Override
            public void onStreakUpdated(int newStreak) {
                runOnUiThread(() -> streakDays.setText(String.valueOf(newStreak)));
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> streakDays.setText("1"));
            }
        });

        SharedPreferences prefs = getSharedPreferences("game_data", MODE_PRIVATE);
        long vidas = prefs.getLong("vidas", 5);
        livesCount.setText(String.valueOf(vidas));
        diamondsCount.setText("0");
    }

    // ══════════════════════════════════════════════════════════════
    // STICKY HEADER
    // ══════════════════════════════════════════════════════════════
    private void setupStickyHeader() {
        if (stickySection == null) return;
        int stickyColor = ContextCompat.getColor(this, R.color.rosado);

        stickySection.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        stickySectionTop = stickySection.getTop();
                        stickySection.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        View scroll = findViewById(R.id.mainScrollView);
                        if (scroll != null) {
                            scroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
                                int scrollY = scroll.getScrollY();
                                if (sectionContent != null) {
                                    sectionContent.setBackgroundColor(
                                            scrollY >= stickySectionTop ? stickyColor : 0x00000000
                                    );
                                }
                            });
                        }
                    }
                });
    }

    // ══════════════════════════════════════════════════════════════
    // MAPA DE VARIANTES
    // ══════════════════════════════════════════════════════════════
    private void setupMapaButton() {
        View.OnClickListener abrirMapa = v -> {
            if (cardMapaVariantes != null) {
                cardMapaVariantes.animate()
                        .scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(() -> cardMapaVariantes.animate()
                                .scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();
            }
            startActivity(new Intent(this, MapaVariantesActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        };

        if (cardMapaVariantes != null) cardMapaVariantes.setOnClickListener(abrirMapa);
        if (ivMapaQuechua     != null) ivMapaQuechua.setOnClickListener(abrirMapa);
    }

    // ══════════════════════════════════════════════════════════════
    // BURBUJAS FLOTANTES
    // ══════════════════════════════════════════════════════════════
    private void setupBurbujas() {
        if (draggableBubble  != null) setupDraggableBubble(draggableBubble,  true);
        if (curiositiesBubble != null) setupDraggableBubble(curiositiesBubble, false);
    }

    private void setupDraggableBubble(CardView bubble, boolean isReview) {
        bubble.setOnTouchListener(new View.OnTouchListener() {
            private boolean isDragging = false;
            private float localDX, localDY;
            private static final int THRESHOLD = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        localDX = v.getX() - event.getRawX();
                        localDY = v.getY() - event.getRawY();
                        isDragging = false;
                        v.setElevation(20f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getRawX() + localDX - v.getX());
                        float dy = Math.abs(event.getRawY() + localDY - v.getY());
                        if (dx > THRESHOLD || dy > THRESHOLD) isDragging = true;

                        float newX = event.getRawX() + localDX;
                        float newY = event.getRawY() + localDY;
                        ViewGroup parent = (ViewGroup) v.getParent();
                        newX = Math.max(0, Math.min(newX, parent.getWidth()  - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, parent.getHeight() - v.getHeight()));
                        v.animate().x(newX).y(newY).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setElevation(16f);
                        if (!isDragging) {
                            if (isReview) onReviewBubbleClicked();
                            else          onCuriositiesBubbleClicked();
                        }
                        break;
                    default: return false;
                }
                return true;
            }
        });
    }

    private void onReviewBubbleClicked() {
        try {
            startActivity(new Intent(this, ReviewActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Función próximamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCuriositiesBubbleClicked() {
        try {
            startActivity(new Intent(this, CuriositiesActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Función próximamente", Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // TOAST BIENVENIDA
    // ══════════════════════════════════════════════════════════════
    private void mostrarToastBienvenida() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String rol = getIntent().getStringExtra("user_role");
        if (rol == null) rol = prefs.getString("user_role", "usuario_comun");

        String msg;
        switch (rol) {
            case "administrador": msg = "👑 Bienvenido, Administrador"; break;
            case "docente":       msg = "📚 Bienvenido, Docente";       break;
            default:              msg = "👋 Bienvenido, Aprendiz";      break;
        }

        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
    }

    // ══════════════════════════════════════════════════════════════
    // FUENTES
    // ══════════════════════════════════════════════════════════════
    private void aplicarFuentesAutomaticas() {
        aplicarFuente(R.id.streakDays,    16f);
        aplicarFuente(R.id.diamondsCount, 16f);
        aplicarFuente(R.id.livesCount,    16f);
    }

    private void aplicarFuente(int id, float size) {
        TextView tv = findViewById(id);
        if (tv != null) configuracionActivity.aplicarTamanioFuente(tv, size);
    }

    // ══════════════════════════════════════════════════════════════
    // BOTTOM NAV
    // ══════════════════════════════════════════════════════════════
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        if (nav == null) return;
        nav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)       return true;
            if (id == R.id.nav_dictionary) { startActivity(new Intent(this, HerramientasActivity.class)); finish(); return true; }
            if (id == R.id.nav_Minijuegos) { startActivity(new Intent(this, MiniJuegosActivity.class));   finish(); return true; }
            if (id == R.id.nav_profile)    { startActivity(new Intent(this, perfilActivity.class));        finish(); return true; }
            return false;
        });
    }

    @Override
    protected int getSelectedNavItemId() { return R.id.nav_home; }

    // ══════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ══════════════════════════════════════════════════════════════
    @Override
    protected void onResume() {
        super.onResume();
        FloatingChatManager.attach(this);
        updateStreakAndData();
        aplicarFuentesAutomaticas();
        // Las lecciones se actualizan solas por el listener en tiempo real
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FloatingChatManager.detach();
        if (ttsManager != null) ttsManager.detener();
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════
    private String getStr(DataSnapshot ds, String key, String def) {
        String v = ds.child(key).getValue(String.class);
        return v != null ? v : def;
    }

    // ══════════════════════════════════════════════════════════════
    // MODELO DE DATOS
    // ══════════════════════════════════════════════════════════════
    public static class LeccionCard {
        public String  id;
        public String  titulo;
        public String  descripcion;
        public String  nivel;
        public String  categoria;
        public int     numEjercicios;
        public int     exp;
        public long    createdAt;
        public boolean completada;
        public boolean disponible;
    }

    // ══════════════════════════════════════════════════════════════
    // ADAPTER DE LECCIONES
    // ══════════════════════════════════════════════════════════════
    public static class LeccionesAdapter
            extends RecyclerView.Adapter<LeccionesAdapter.VH> {

        private final List<LeccionCard> lista;
        private final homeActivity      ctx;

        LeccionesAdapter(List<LeccionCard> lista, homeActivity ctx) {
            this.lista = lista;
            this.ctx   = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leccion_estudiante, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            LeccionCard c = lista.get(pos);

            h.tvTitulo.setText(c.titulo);
            h.tvCategoria.setText(c.categoria);
            h.tvNivel.setText(c.nivel);
            h.tvEjercicios.setText(c.numEjercicios + " ejercicios");
            h.tvExp.setText("+" + c.exp + " EXP");

            // Estado visual
            if (c.completada) {
                h.tvEstado.setText("✅ Completada");
                h.tvEstado.setTextColor(Color.parseColor("#27AE60"));
                h.ivCandado.setVisibility(View.GONE);
                h.cardLeccion.setAlpha(1f);
                h.cardLeccion.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
                h.indicadorEstado.setBackgroundColor(Color.parseColor("#27AE60"));

            } else if (c.disponible) {
                h.tvEstado.setText("▶ Disponible");
                h.tvEstado.setTextColor(Color.parseColor("#FF6F00"));
                h.ivCandado.setVisibility(View.GONE);
                h.cardLeccion.setAlpha(1f);
                h.cardLeccion.setCardBackgroundColor(Color.WHITE);
                h.indicadorEstado.setBackgroundColor(Color.parseColor("#FF6F00"));

            } else {
                h.tvEstado.setText("🔒 Bloqueada");
                h.tvEstado.setTextColor(Color.parseColor("#9E9E9E"));
                h.ivCandado.setVisibility(View.VISIBLE);
                h.cardLeccion.setAlpha(0.6f);
                h.cardLeccion.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
                h.indicadorEstado.setBackgroundColor(Color.parseColor("#BDBDBD"));
            }

            // Color del nivel
            int nivelColor;
            switch (c.nivel) {
                case "Intermedio": nivelColor = Color.parseColor("#F39C12"); break;
                case "Avanzado":   nivelColor = Color.parseColor("#E74C3C"); break;
                default:           nivelColor = Color.parseColor("#27AE60"); break;
            }
            h.tvNivel.setBackgroundColor(nivelColor);

            // Click
            h.cardLeccion.setOnClickListener(v -> ctx.abrirLeccion(c));

            // Animación entrada
            h.itemView.setAlpha(0f);
            h.itemView.setTranslationX(40f);
            h.itemView.animate()
                    .alpha(1f).translationX(0f)
                    .setDuration(350)
                    .setStartDelay(pos * 60L)
                    .start();
        }

        @Override
        public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            CardView  cardLeccion;
            TextView  tvTitulo, tvCategoria, tvNivel, tvEjercicios, tvExp, tvEstado;
            ImageView ivCandado;
            View      indicadorEstado;

            VH(View v) {
                super(v);
                cardLeccion     = v.findViewById(R.id.cardLeccionEstudiante);
                tvTitulo        = v.findViewById(R.id.tvTituloLeccionEst);
                tvCategoria     = v.findViewById(R.id.tvCategoriaLeccionEst);
                tvNivel         = v.findViewById(R.id.tvNivelLeccionEst);
                tvEjercicios    = v.findViewById(R.id.tvEjerciciosLeccionEst);
                tvExp           = v.findViewById(R.id.tvExpLeccionEst);
                tvEstado        = v.findViewById(R.id.tvEstadoLeccionEst);
                ivCandado       = v.findViewById(R.id.ivCandadoLeccion);
                indicadorEstado = v.findViewById(R.id.indicadorEstadoLeccion);
            }
        }
    }
}