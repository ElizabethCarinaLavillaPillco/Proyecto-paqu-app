package com.example.paqu;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class GestionPalabrasDocenteActivity extends AppCompatActivity {

    private static final String TAG       = "GestionPalabras";
    private static final String FILE_DICT = "diccionario_local.json";

    // Vistas
    private RecyclerView  rvPalabras;
    private PalabraAdapter adapter;
    private List<PalabraItem> todasLasPalabras;
    private List<PalabraItem> palabrasFiltradas;

    private EditText    etBuscarPalabra;
    private ChipGroup   chipGroupCategorias;
    private TextView    tvContadorPalabras;
    private LinearLayout layoutVacio;
    private FloatingActionButton fabAgregarPalabra;
    private ImageView   btnBack;
    private TextView    tvTituloGestion;

    private DatabaseReference dbRef;
    private String docenteUid;
    private String categoriaFiltro = "Todas";

    // Categorías disponibles
    private static final String[] CATEGORIAS = {
            "Saludos", "Familia", "Naturaleza", "Números",
            "Colores", "Animales", "Verbos", "Frases", "Otra"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_palabras_docente);

        docenteUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecycler();
        setupListeners();
        cargarPalabras();
    }

    // ─────────────────────────────────────────────
    // VISTAS
    // ─────────────────────────────────────────────
    private void initViews() {
        rvPalabras         = findViewById(R.id.rvPalabrasDocente);
        etBuscarPalabra    = findViewById(R.id.etBuscarPalabraDocente);
        chipGroupCategorias = findViewById(R.id.chipGroupCategoriasDocente);
        tvContadorPalabras = findViewById(R.id.tvContadorPalabrasDocente);
        layoutVacio        = findViewById(R.id.layoutVacioPalabras);
        fabAgregarPalabra  = findViewById(R.id.fabAgregarPalabra);
        btnBack            = findViewById(R.id.btnBack);
        tvTituloGestion    = findViewById(R.id.tvTituloGestion);

        tvTituloGestion.setText("📖 Diccionario Quechua");
    }

    // ─────────────────────────────────────────────
    // RECYCLER
    // ─────────────────────────────────────────────
    private void setupRecycler() {
        todasLasPalabras  = new ArrayList<>();
        palabrasFiltradas = new ArrayList<>();
        adapter = new PalabraAdapter(palabrasFiltradas, this);
        rvPalabras.setLayoutManager(new LinearLayoutManager(this));
        rvPalabras.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────
    // LISTENERS
    // ─────────────────────────────────────────────
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        fabAgregarPalabra.setOnClickListener(v -> {
            animarFab(v);
            mostrarDialogoAgregarPalabra(null);
        });

        etBuscarPalabra.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filtrar(s.toString());
            }
        });

        // Chips de categoría
        for (int i = 0; i < chipGroupCategorias.getChildCount(); i++) {
            View child = chipGroupCategorias.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    categoriaFiltro = chip.getText().toString();
                    filtrar(etBuscarPalabra.getText().toString());
                });
            }
        }
    }

    // ─────────────────────────────────────────────
    // CARGAR PALABRAS: FIREBASE + LOCAL
    // ─────────────────────────────────────────────
    private void cargarPalabras() {
        // 1. Cargar desde archivo local primero (offline inmediato)
        cargarDesdeLocal();

        // 2. Luego sincronizar con Firebase
        dbRef.child("diccionario")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        todasLasPalabras.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PalabraItem p = new PalabraItem();
                            p.id           = ds.getKey();
                            p.quechua      = getStr(ds, "quechua",       "");
                            p.espanol      = getStr(ds, "espanol",       "");
                            p.pronunciacion = getStr(ds, "pronunciacion", "");
                            p.categoria    = getStr(ds, "categoria",     "Otra");
                            p.creadoPor    = getStr(ds, "creadoPor",     "");
                            p.esMia        = docenteUid.equals(p.creadoPor);

                            Long ts = ds.child("creadoEn").getValue(Long.class);
                            p.creadoEn = ts != null ? ts : 0L;

                            if (!p.quechua.isEmpty()) todasLasPalabras.add(p);
                        }

                        // Ordenar: primero las mías, luego alfabéticamente
                        todasLasPalabras.sort((a, b) -> {
                            if (a.esMia && !b.esMia) return -1;
                            if (!a.esMia && b.esMia)  return 1;
                            return a.quechua.compareToIgnoreCase(b.quechua);
                        });

                        // Guardar localmente para modo offline
                        guardarEnLocal(todasLasPalabras);

                        filtrar(etBuscarPalabra.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase error: " + error.getMessage());
                        Toast.makeText(GestionPalabrasDocenteActivity.this,
                                "Mostrando datos locales", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─────────────────────────────────────────────
    // OFFLINE: LEER/ESCRIBIR JSON LOCAL
    // ─────────────────────────────────────────────
    private void cargarDesdeLocal() {
        try {
            File file = new File(getFilesDir(), FILE_DICT);
            if (!file.exists()) return;

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONArray arr = new JSONArray(sb.toString());
            todasLasPalabras.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                PalabraItem p = new PalabraItem();
                p.id            = obj.optString("id");
                p.quechua       = obj.optString("quechua");
                p.espanol       = obj.optString("espanol");
                p.pronunciacion = obj.optString("pronunciacion");
                p.categoria     = obj.optString("categoria", "Otra");
                p.creadoPor     = obj.optString("creadoPor");
                p.esMia         = docenteUid.equals(p.creadoPor);
                p.creadoEn      = obj.optLong("creadoEn");
                todasLasPalabras.add(p);
            }

            filtrar("");
            Log.d(TAG, "Cargadas " + todasLasPalabras.size() + " palabras del local");

        } catch (Exception e) {
            Log.e(TAG, "Error local: " + e.getMessage());
        }
    }

    private void guardarEnLocal(List<PalabraItem> lista) {
        try {
            JSONArray arr = new JSONArray();
            for (PalabraItem p : lista) {
                JSONObject obj = new JSONObject();
                obj.put("id",            p.id);
                obj.put("quechua",       p.quechua);
                obj.put("espanol",       p.espanol);
                obj.put("pronunciacion", p.pronunciacion);
                obj.put("categoria",     p.categoria);
                obj.put("creadoPor",     p.creadoPor);
                obj.put("creadoEn",      p.creadoEn);
                arr.put(obj);
            }

            FileWriter fw = new FileWriter(new File(getFilesDir(), FILE_DICT));
            fw.write(arr.toString());
            fw.close();

        } catch (Exception e) {
            Log.e(TAG, "Error guardando local: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // FILTRAR
    // ─────────────────────────────────────────────
    private void filtrar(String query) {
        palabrasFiltradas.clear();

        for (PalabraItem p : todasLasPalabras) {
            boolean matchQ = query.isEmpty()
                    || p.quechua.toLowerCase().contains(query.toLowerCase())
                    || p.espanol.toLowerCase().contains(query.toLowerCase());

            boolean matchC = categoriaFiltro.equals("Todas")
                    || p.categoria.equals(categoriaFiltro);

            if (matchQ && matchC) palabrasFiltradas.add(p);
        }

        adapter.notifyDataSetChanged();
        layoutVacio.setVisibility(palabrasFiltradas.isEmpty() ? View.VISIBLE : View.GONE);
        rvPalabras.setVisibility(palabrasFiltradas.isEmpty() ? View.GONE : View.VISIBLE);
        tvContadorPalabras.setText(palabrasFiltradas.size() + " palabras");
    }

    // ─────────────────────────────────────────────
    // 🔹 DIÁLOGO: AGREGAR / EDITAR PALABRA (CORREGIDO)
    // ─────────────────────────────────────────────
    private void mostrarDialogoAgregarPalabra(PalabraItem existente) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_palabra, null);

        EditText etQuechua       = dialogView.findViewById(R.id.etPalabraQuechua);
        EditText etEspanol       = dialogView.findViewById(R.id.etPalabraEspanol);
        EditText etPronunciacion = dialogView.findViewById(R.id.etPronunciacion);
        // 🔹 CAMBIO: Ahora es MaterialAutoCompleteTextView en lugar de Spinner
        MaterialAutoCompleteTextView spinnerCat = dialogView.findViewById(R.id.spinnerCategoriaWord);
        TextView tvDialogoTitulo = dialogView.findViewById(R.id.tvDialogoPalabraTitulo);

        // 🔹 CAMBIO: Configurar adapter para MaterialAutoCompleteTextView
        ArrayAdapter<String> adCat = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIAS);
        spinnerCat.setAdapter(adCat);
        // 🔹 Establecer valor por defecto
        spinnerCat.setText(CATEGORIAS[0], false);

        // Si es edición, precargar datos
        boolean esEdicion = existente != null;
        if (esEdicion) {
            tvDialogoTitulo.setText("✏️ Editar Palabra");
            etQuechua.setText(existente.quechua);
            etEspanol.setText(existente.espanol);
            etPronunciacion.setText(existente.pronunciacion);
            // 🔹 CAMBIO: Usar setText() en lugar de setSelection()
            spinnerCat.setText(existente.categoria, false);
        } else {
            tvDialogoTitulo.setText("➕ Nueva Palabra");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String quechua       = etQuechua.getText().toString().trim();
            String espanol       = etEspanol.getText().toString().trim();
            String pronunciacion = etPronunciacion.getText().toString().trim();
            // 🔹 CAMBIO: Usar getText() en lugar de getSelectedItem()
            String categoria     = spinnerCat.getText().toString().trim();

            if (quechua.isEmpty() || espanol.isEmpty()) {
                Toast.makeText(this, "La palabra Quechua y Español son obligatorias",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (pronunciacion.isEmpty()) pronunciacion = quechua.toLowerCase();

            guardarPalabra(
                    esEdicion ? existente.id : null,
                    quechua, espanol, pronunciacion, categoria
            );
            dialog.dismiss();
        });
    }

    // ─────────────────────────────────────────────
    // GUARDAR EN FIREBASE
    // ─────────────────────────────────────────────
    private void guardarPalabra(String existingId, String quechua,
                                String espanol, String pronunciacion, String categoria) {
        String wordId = existingId != null
                ? existingId
                : dbRef.child("diccionario").push().getKey();

        if (wordId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("quechua",       quechua);
        data.put("espanol",       espanol);
        data.put("pronunciacion", pronunciacion);
        data.put("categoria",     categoria);
        data.put("creadoPor",     docenteUid);
        data.put("creadoEn",      System.currentTimeMillis());

        dbRef.child("diccionario").child(wordId).setValue(data)
                .addOnSuccessListener(a -> {
                    String msg = existingId != null ? "✅ Palabra actualizada" : "✅ Palabra agregada";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ─────────────────────────────────────────────
    // ELIMINAR PALABRA (SOLO LAS PROPIAS)
    // ─────────────────────────────────────────────
    public void confirmarEliminarPalabra(PalabraItem p) {
        if (!p.esMia) {
            Toast.makeText(this, "Solo puedes eliminar tus propias palabras", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar palabra?")
                .setMessage("\"" + p.quechua + "\" se eliminará del diccionario.")
                .setPositiveButton("Eliminar", (d, w) ->
                        dbRef.child("diccionario").child(p.id).removeValue()
                                .addOnSuccessListener(a ->
                                        Toast.makeText(this, "Palabra eliminada", Toast.LENGTH_SHORT).show()
                                )
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    private String getStr(DataSnapshot ds, String key, String def) {
        String v = ds.child(key).getValue(String.class);
        return v != null ? v : def;
    }

    private void animarFab(View v) {
        v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                .start();
    }

    // ─────────────────────────────────────────────
    // MODELO
    // ─────────────────────────────────────────────
    public static class PalabraItem {
        public String id;
        public String quechua;
        public String espanol;
        public String pronunciacion;
        public String categoria;
        public String creadoPor;
        public long   creadoEn;
        public boolean esMia;
    }

    // ─────────────────────────────────────────────
    // ADAPTER
    // ─────────────────────────────────────────────
    public static class PalabraAdapter
            extends RecyclerView.Adapter<PalabraAdapter.VH> {

        private final List<PalabraItem> lista;
        private final GestionPalabrasDocenteActivity ctx;

        PalabraAdapter(List<PalabraItem> lista, GestionPalabrasDocenteActivity ctx) {
            this.lista = lista;
            this.ctx   = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_palabra_docente, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            PalabraItem p = lista.get(pos);

            h.tvQuechua.setText(p.quechua);
            h.tvEspanol.setText(p.espanol);
            h.tvPronunciacion.setText("🔊 " + p.pronunciacion);
            h.tvCategoria.setText(p.categoria);

            // Indicador: mi palabra vs de otro docente
            if (p.esMia) {
                h.tvAutor.setText("✏️ Yo");
                h.tvAutor.setTextColor(Color.parseColor("#E67E22"));
                h.btnEliminar.setVisibility(View.VISIBLE);
                h.btnEditar.setVisibility(View.VISIBLE);
            } else {
                h.tvAutor.setText("👤 Otro docente");
                h.tvAutor.setTextColor(Color.parseColor("#95A5A6"));
                h.btnEliminar.setVisibility(View.GONE);
                h.btnEditar.setVisibility(View.GONE);
            }

            // Color categoría
            h.indicadorCategoria.setBackgroundColor(colorParaCategoria(p.categoria));

            // Botones
            h.btnEliminar.setOnClickListener(v -> ctx.confirmarEliminarPalabra(p));
            h.btnEditar.setOnClickListener(v -> ctx.mostrarDialogoAgregarPalabra(p));

            // Animación
            h.itemView.setAlpha(0f);
            h.itemView.animate().alpha(1f)
                    .setDuration(280)
                    .setStartDelay(pos * 40L)
                    .start();
        }

        private int colorParaCategoria(String cat) {
            switch (cat) {
                case "Saludos":   return Color.parseColor("#9B59B6");
                case "Familia":   return Color.parseColor("#E91E63");
                case "Naturaleza": return Color.parseColor("#27AE60");
                case "Números":   return Color.parseColor("#3498DB");
                case "Colores":   return Color.parseColor("#F39C12");
                case "Animales":  return Color.parseColor("#E67E22");
                case "Verbos":    return Color.parseColor("#1ABC9C");
                case "Frases":    return Color.parseColor("#8E44AD");
                default:          return Color.parseColor("#7F8C8D");
            }
        }

        @Override
        public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView  tvQuechua, tvEspanol, tvPronunciacion, tvCategoria, tvAutor;
            ImageView btnEliminar, btnEditar;
            View      indicadorCategoria;

            VH(View v) {
                super(v);
                tvQuechua        = v.findViewById(R.id.tvPalabraQuechua);
                tvEspanol        = v.findViewById(R.id.tvPalabraEspanol);
                tvPronunciacion  = v.findViewById(R.id.tvPronunciacionPalabra);
                tvCategoria      = v.findViewById(R.id.tvCategoriaPalabra);
                tvAutor          = v.findViewById(R.id.tvAutorPalabra);
                btnEliminar      = v.findViewById(R.id.btnEliminarPalabra);
                btnEditar        = v.findViewById(R.id.btnEditarPalabra);
                indicadorCategoria = v.findViewById(R.id.indicadorCategoriaPalabra);
            }
        }
    }
}