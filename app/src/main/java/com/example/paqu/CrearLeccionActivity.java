package com.example.paqu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class CrearLeccionActivity extends AppCompatActivity {

    private static final String TAG = "CrearLeccion";

    // Tipos de ejercicio
    public static final String TIPO_SELECCION  = "SELECCION_MULTIPLE";
    public static final String TIPO_FRASE      = "CONSTRUCCION_FRASE";
    public static final String TIPO_ESCRITURA  = "ESCRITURA_LIBRE";

    // Vistas del formulario principal
    private EditText etTituloLeccion;
    private EditText etDescripcionLeccion;
    // 🔹 CAMBIO: Ahora son AutoCompleteTextView en lugar de Spinner
    private MaterialAutoCompleteTextView spinnerNivel;
    private MaterialAutoCompleteTextView spinnerCategoria;
    private EditText etExpRecompensa;
    private Button   btnGuardarLeccion;
    private ImageView btnBack;

    // Lista de ejercicios
    private RecyclerView rvEjercicios;
    private EjercicioCreadorAdapter adapter;
    private List<EjercicioCreador> listaEjercicios;

    // Botón agregar ejercicio
    private CardView cardAgregarEjercicio;

    private DatabaseReference dbRef;
    private String docenteUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_leccion2);

        docenteUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupSpinners();
        setupRecyclerView();
        setupListeners();
        animarEntrada();
    }

    // ─────────────────────────────────────────────
    // INICIALIZAR VISTAS
    // ─────────────────────────────────────────────
    @SuppressLint("WrongViewCast")
    private void initViews() {
        etTituloLeccion      = findViewById(R.id.etTituloLeccion);
        etDescripcionLeccion = findViewById(R.id.etDescripcionLeccion);
        // 🔹 CAMBIO: Castear a MaterialAutoCompleteTextView
        spinnerNivel         = findViewById(R.id.spinnerNivel);
        spinnerCategoria     = findViewById(R.id.spinnerCategoria);
        etExpRecompensa      = findViewById(R.id.etExpRecompensa);
        btnGuardarLeccion    = findViewById(R.id.btnGuardarLeccion);
        btnBack              = findViewById(R.id.btnBack);
        rvEjercicios         = findViewById(R.id.rvEjerciciosCreados);
        cardAgregarEjercicio = findViewById(R.id.cardAgregarEjercicio);
    }

    // ─────────────────────────────────────────────
    // 🔹 SPINNERS (AHORA CON AutoCompleteTextView)
    // ─────────────────────────────────────────────
    private void setupSpinners() {
        // Spinner nivel
        String[] niveles = {"Básico", "Intermedio", "Avanzado"};
        ArrayAdapter<String> adNivel = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, niveles);
        spinnerNivel.setAdapter(adNivel);
        // 🔹 Establecer valor por defecto
        spinnerNivel.setText("Básico", false);

        // Spinner categoría
        String[] categorias = {"Saludos", "Familia", "Números", "Colores",
                "Animales", "Naturaleza", "Verbos", "Frases", "Otra"};
        ArrayAdapter<String> adCat = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categorias);
        spinnerCategoria.setAdapter(adCat);
        // 🔹 Establecer valor por defecto
        spinnerCategoria.setText("Saludos", false);
    }

    // ─────────────────────────────────────────────
    // RECYCLER VIEW DE EJERCICIOS
    // ─────────────────────────────────────────────
    private void setupRecyclerView() {
        listaEjercicios = new ArrayList<>();
        adapter = new EjercicioCreadorAdapter(listaEjercicios, this);
        rvEjercicios.setLayoutManager(new LinearLayoutManager(this));
        rvEjercicios.setAdapter(adapter);

        // Drag para reordenar
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to   = target.getAdapterPosition();
                Collections.swap(listaEjercicios, from, to);
                adapter.notifyItemMoved(from, to);
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {}
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rvEjercicios);
    }

    // ─────────────────────────────────────────────
    // LISTENERS
    // ─────────────────────────────────────────────
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Agregar nuevo ejercicio → diálogo de selección de tipo
        cardAgregarEjercicio.setOnClickListener(v -> mostrarDialogoTipoEjercicio());

        // Guardar lección en Firebase
        btnGuardarLeccion.setOnClickListener(v -> validarYGuardar());
    }

    // ─────────────────────────────────────────────
    // DIÁLOGO: ELEGIR TIPO DE EJERCICIO
    // ─────────────────────────────────────────────
    private void mostrarDialogoTipoEjercicio() {
        String[] tipos = {
                "🎵 Selección múltiple  (escuchar y elegir)",
                "🧩 Construir frase     (ordenar palabras)",
                "✏️ Escritura libre     (escribir la palabra)"
        };

        new AlertDialog.Builder(this)
                .setTitle("¿Qué tipo de ejercicio?")
                .setItems(tipos, (dialog, which) -> {
                    switch (which) {
                        case 0: mostrarFormularioEjercicio(TIPO_SELECCION); break;
                        case 1: mostrarFormularioEjercicio(TIPO_FRASE);     break;
                        case 2: mostrarFormularioEjercicio(TIPO_ESCRITURA); break;
                    }
                })
                .show();
    }

    // ─────────────────────────────────────────────
    // DIÁLOGO: FORMULARIO SEGÚN TIPO
    // ─────────────────────────────────────────────
    private void mostrarFormularioEjercicio(String tipo) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_crear_ejercicio, null);

        // Campos comunes
        TextView tvTipoLabel    = dialogView.findViewById(R.id.tvTipoEjercicioLabel);
        EditText etPregunta     = dialogView.findViewById(R.id.etPreguntaEjercicio);
        EditText etRespuesta    = dialogView.findViewById(R.id.etRespuestaCorrecta);
        EditText etPalabraAudio = dialogView.findViewById(R.id.etPalabraAudio);

        // Sección opciones (solo para SELECCION y FRASE)
        LinearLayout layoutOpciones = dialogView.findViewById(R.id.layoutOpciones);
        EditText etOpcion1 = dialogView.findViewById(R.id.etOpcion1);
        EditText etOpcion2 = dialogView.findViewById(R.id.etOpcion2);
        EditText etOpcion3 = dialogView.findViewById(R.id.etOpcion3);
        EditText etOpcion4 = dialogView.findViewById(R.id.etOpcion4);

        // Configurar según tipo
        switch (tipo) {
            case TIPO_SELECCION:
                tvTipoLabel.setText("🎵 Selección Múltiple");
                etPregunta.setHint("¿Qué significa esta palabra? (ej: Buenos días)");
                etRespuesta.setHint("Respuesta correcta en Quechua (ej: Allin p'unchay)");
                layoutOpciones.setVisibility(View.VISIBLE);
                break;
            case TIPO_FRASE:
                tvTipoLabel.setText("🧩 Construir Frase");
                etPregunta.setHint("Instrucción (ej: Ordena las palabras para decir 'Buenos días')");
                etRespuesta.setHint("Frase correcta (ej: Allin p'unchay)");
                layoutOpciones.setVisibility(View.VISIBLE);
                etOpcion1.setHint("Palabra 1 (ej: Allin)");
                etOpcion2.setHint("Palabra 2 (ej: p'unchay)");
                etOpcion3.setHint("Distractor 1 (ej: tuta)");
                etOpcion4.setHint("Distractor 2 (ej: samay)");
                break;
            case TIPO_ESCRITURA:
                tvTipoLabel.setText("✏️ Escritura Libre");
                etPregunta.setHint("Descripción en español (ej: ¿Cómo se dice 'hasta mañana'?)");
                etRespuesta.setHint("Respuesta en Quechua (ej: Paqarinkama)");
                layoutOpciones.setVisibility(View.GONE);
                break;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nuevo Ejercicio")
                .setView(dialogView)
                .setPositiveButton("Agregar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        // Sobreescribir el positivo para validar antes de cerrar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pregunta    = etPregunta.getText().toString().trim();
            String respuesta   = etRespuesta.getText().toString().trim();
            String palabraAudi = etPalabraAudio.getText().toString().trim();

            if (pregunta.isEmpty() || respuesta.isEmpty()) {
                Toast.makeText(this, "Completa la pregunta y la respuesta", Toast.LENGTH_SHORT).show();
                return;
            }

            // Construir objeto ejercicio
            EjercicioCreador ej = new EjercicioCreador();
            ej.tipo             = tipo;
            ej.pregunta         = pregunta;
            ej.respuestaCorrecta = respuesta;
            ej.palabraAudio     = palabraAudi.isEmpty() ? respuesta : palabraAudi;

            if (tipo.equals(TIPO_SELECCION) || tipo.equals(TIPO_FRASE)) {
                ej.opciones = new ArrayList<>();
                if (!etOpcion1.getText().toString().trim().isEmpty())
                    ej.opciones.add(etOpcion1.getText().toString().trim());
                if (!etOpcion2.getText().toString().trim().isEmpty())
                    ej.opciones.add(etOpcion2.getText().toString().trim());
                if (!etOpcion3.getText().toString().trim().isEmpty())
                    ej.opciones.add(etOpcion3.getText().toString().trim());
                if (!etOpcion4.getText().toString().trim().isEmpty())
                    ej.opciones.add(etOpcion4.getText().toString().trim());

                if (ej.opciones.size() < 2) {
                    Toast.makeText(this, "Agrega al menos 2 opciones", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Asegurarse de que la respuesta correcta esté en las opciones
                if (!ej.opciones.contains(ej.respuestaCorrecta)) {
                    ej.opciones.add(0, ej.respuestaCorrecta);
                }

                // Mezclar opciones
                Collections.shuffle(ej.opciones);
            }

            listaEjercicios.add(ej);
            adapter.notifyItemInserted(listaEjercicios.size() - 1);
            actualizarContadorEjercicios();
            dialog.dismiss();

            Toast.makeText(this, "✅ Ejercicio agregado", Toast.LENGTH_SHORT).show();
        });
    }

    // ─────────────────────────────────────────────
    // VALIDAR Y GUARDAR EN FIREBASE
    // ─────────────────────────────────────────────
    private void validarYGuardar() {
        String titulo     = etTituloLeccion.getText().toString().trim();
        String descripcion = etDescripcionLeccion.getText().toString().trim();
        // 🔹 CAMBIO: Ahora usamos getText() en lugar de getSelectedItem()
        String nivel      = spinnerNivel.getText().toString().trim();
        String categoria  = spinnerCategoria.getText().toString().trim();
        String expStr     = etExpRecompensa.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTituloLeccion.setError("El título es obligatorio");
            etTituloLeccion.requestFocus();
            return;
        }

        if (listaEjercicios.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        int exp = expStr.isEmpty() ? 30 : Integer.parseInt(expStr);

        // Mostrar progreso
        btnGuardarLeccion.setEnabled(false);
        btnGuardarLeccion.setText("Guardando...");

        // Construir mapa de Firebase
        String lessonId = dbRef.child("lessons").push().getKey();
        if (lessonId == null) {
            Toast.makeText(this, "Error al generar ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> leccionData = new HashMap<>();

        // lessonInfo
        Map<String, Object> lessonInfo = new HashMap<>();
        lessonInfo.put("id",          lessonId);
        lessonInfo.put("title",       titulo);
        lessonInfo.put("description", descripcion);
        lessonInfo.put("nivel",       nivel);
        lessonInfo.put("categoria",   categoria);
        lessonInfo.put("createdBy",   docenteUid);
        lessonInfo.put("createdAt",   System.currentTimeMillis());
        lessonInfo.put("activa",      true);
        leccionData.put("lessonInfo", lessonInfo);

        // content/ejercicios
        List<Map<String, Object>> ejerciciosList = new ArrayList<>();
        for (int i = 0; i < listaEjercicios.size(); i++) {
            EjercicioCreador ej = listaEjercicios.get(i);
            Map<String, Object> ejMap = new HashMap<>();
            ejMap.put("index",            i);
            ejMap.put("tipo",             ej.tipo);
            ejMap.put("pregunta",         ej.pregunta);
            ejMap.put("respuestaCorrecta", ej.respuestaCorrecta);
            ejMap.put("palabraAudio",     ej.palabraAudio);
            if (ej.opciones != null) ejMap.put("opciones", ej.opciones);
            ejerciciosList.add(ejMap);
        }

        Map<String, Object> content = new HashMap<>();
        content.put("ejercicios", ejerciciosList);
        leccionData.put("content", content);

        // rewards
        Map<String, Object> rewards = new HashMap<>();
        rewards.put("exp",     exp);
        rewards.put("monedas", 10);
        leccionData.put("rewards", rewards);

        // Guardar en Firebase
        dbRef.child("lessons").child(lessonId).setValue(leccionData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Lección guardada: " + lessonId);
                    mostrarExito(titulo);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar: " + e.getMessage());
                    btnGuardarLeccion.setEnabled(true);
                    btnGuardarLeccion.setText("Guardar Lección");
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarExito(String titulo) {
        new AlertDialog.Builder(this)
                .setTitle("✅ ¡Lección Creada!")
                .setMessage("\"" + titulo + "\" está lista para los estudiantes.")
                .setPositiveButton("Ver mis lecciones", (d, w) -> {
                    startActivity(new Intent(this, MisLeccionesActivity.class));
                    finish();
                })
                .setNegativeButton("Crear otra", (d, w) -> {
                    recreate();
                })
                .setCancelable(false)
                .show();
    }

    private void actualizarContadorEjercicios() {
        TextView tvContador = findViewById(R.id.tvContadorEjercicios);
        if (tvContador != null) {
            tvContador.setText(listaEjercicios.size() + " ejercicio(s)");
        }
    }

    // ─────────────────────────────────────────────
    // ANIMACIÓN DE ENTRADA
    // ─────────────────────────────────────────────
    private void animarEntrada() {
        View formCard = findViewById(R.id.cardFormLeccion);
        if (formCard != null) {
            formCard.setAlpha(0f);
            formCard.setTranslationY(60f);
            formCard.animate()
                    .alpha(1f).translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    // ─────────────────────────────────────────────
    // MODELO DE DATOS (clase interna)
    // ─────────────────────────────────────────────
    public static class EjercicioCreador {
        public String tipo;
        public String pregunta;
        public String respuestaCorrecta;
        public String palabraAudio;
        public List<String> opciones;
    }

    // ─────────────────────────────────────────────
    // ADAPTER PARA LISTA DE EJERCICIOS CREADOS
    // ─────────────────────────────────────────────
    public static class EjercicioCreadorAdapter
            extends RecyclerView.Adapter<EjercicioCreadorAdapter.VH> {

        private final List<EjercicioCreador> lista;
        private final CrearLeccionActivity ctx;

        public EjercicioCreadorAdapter(List<EjercicioCreador> lista, CrearLeccionActivity ctx) {
            this.lista = lista;
            this.ctx   = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ejercicio_creado, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            EjercicioCreador ej = lista.get(pos);

            // Icono según tipo
            String icono;
            switch (ej.tipo) {
                case TIPO_SELECCION: icono = "🎵"; break;
                case TIPO_FRASE:     icono = "🧩"; break;
                default:             icono = "✏️"; break;
            }

            h.tvNumero.setText((pos + 1) + "");
            h.tvTipo.setText(icono + " " + ej.tipo.replace("_", " ").toLowerCase());
            h.tvPregunta.setText(ej.pregunta);
            h.tvRespuesta.setText("✓ " + ej.respuestaCorrecta);

            // Opciones
            if (ej.opciones != null && !ej.opciones.isEmpty()) {
                h.tvOpciones.setVisibility(View.VISIBLE);
                h.tvOpciones.setText("Opciones: " + android.text.TextUtils.join(", ", ej.opciones));
            } else {
                h.tvOpciones.setVisibility(View.GONE);
            }

            // Eliminar
            h.btnEliminar.setOnClickListener(v -> {
                int actualPos = h.getAdapterPosition();
                lista.remove(actualPos);
                notifyItemRemoved(actualPos);
                notifyItemRangeChanged(actualPos, lista.size());
                ctx.actualizarContadorEjercicios();
            });

            // Editar
            h.btnEditar.setOnClickListener(v -> {
                Toast.makeText(ctx, "Edición próximamente", Toast.LENGTH_SHORT).show();
            });

            // Animación de aparición
            h.itemView.setAlpha(0f);
            h.itemView.animate().alpha(1f).setDuration(300).start();
        }

        @Override
        public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvNumero, tvTipo, tvPregunta, tvRespuesta, tvOpciones;
            ImageView btnEliminar, btnEditar;

            VH(View v) {
                super(v);
                tvNumero   = v.findViewById(R.id.tvNumeroEjercicio);
                tvTipo     = v.findViewById(R.id.tvTipoEjercicio);
                tvPregunta = v.findViewById(R.id.tvPreguntaEjercicio);
                tvRespuesta = v.findViewById(R.id.tvRespuestaEjercicio);
                tvOpciones = v.findViewById(R.id.tvOpcionesEjercicio);
                btnEliminar = v.findViewById(R.id.btnEliminarEjercicio);
                btnEditar   = v.findViewById(R.id.btnEditarEjercicio);
            }
        }
    }
}