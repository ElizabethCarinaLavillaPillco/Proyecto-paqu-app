package com.example.paqu;

import android.graphics.Color;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;  // 🔹 IMPORT AGREGADO
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminLeccionesActivity extends AppCompatActivity {

    private static final String TAG = "MisLecciones";

    private RecyclerView     rvLecciones;
    private LeccionAdapter   adapter;
    private List<LeccionItem> todasLasLecciones;
    private List<LeccionItem> leccionesFiltradas;

    private EditText etBuscarLeccion;
    private ChipGroup chipGroupFiltros;
    private TextView tvContadorLecciones;
    private LinearLayout layoutVacio;
    private FloatingActionButton fabCrearLeccion;
    private ImageView btnBack;

    private DatabaseReference dbRef;
    private String docenteUid;
    private String filtroActual = "todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lecciones);

        docenteUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecycler();
        setupListeners();
        cargarLecciones();
    }

    // ─────────────────────────────────────────────
    // VISTAS
    // ─────────────────────────────────────────────
    private void initViews() {
        rvLecciones        = findViewById(R.id.rvMisLecciones);
        etBuscarLeccion    = findViewById(R.id.etBuscarLeccion);
        chipGroupFiltros   = findViewById(R.id.chipGroupFiltrosLecciones);
        tvContadorLecciones = findViewById(R.id.tvContadorMisLecciones);
        layoutVacio        = findViewById(R.id.layoutVacioLecciones);
        fabCrearLeccion    = findViewById(R.id.fabCrearLeccion);
        btnBack            = findViewById(R.id.btnBack);
    }

    // ─────────────────────────────────────────────
    // RECYCLER
    // ─────────────────────────────────────────────
    private void setupRecycler() {
        todasLasLecciones  = new ArrayList<>();
        leccionesFiltradas = new ArrayList<>();
        adapter = new LeccionAdapter(leccionesFiltradas, this);
        rvLecciones.setLayoutManager(new LinearLayoutManager(this));
        rvLecciones.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────
    // LISTENERS
    // ─────────────────────────────────────────────
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        fabCrearLeccion.setOnClickListener(v -> {
            startActivity(new Intent(this, CrearLeccionActivity.class));
        });

        // Búsqueda en tiempo real
        etBuscarLeccion.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filtrar(s.toString());
            }
        });

        // Chips de filtro
        for (int i = 0; i < chipGroupFiltros.getChildCount(); i++) {
            View child = chipGroupFiltros.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    filtroActual = chip.getTag() != null ? chip.getTag().toString() : "todas";
                    filtrar(etBuscarLeccion.getText().toString());
                });
            }
        }
    }

    // ─────────────────────────────────────────────
    // CARGAR LECCIONES DESDE FIREBASE
    // ─────────────────────────────────────────────
    private void cargarLecciones() {
        dbRef.child("lessons")
                .orderByChild("lessonInfo/createdBy")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        todasLasLecciones.clear();

                        for (DataSnapshot ls : snapshot.getChildren()) {
                            try {
                                LeccionItem item = new LeccionItem();
                                item.id = ls.getKey();

                                DataSnapshot info = ls.child("lessonInfo");
                                item.titulo      = getString(info, "title",       "Sin título");
                                item.descripcion = getString(info, "description", "");
                                item.nivel       = getString(info, "nivel",       "Básico");
                                item.categoria   = getString(info, "categoria",   "Otra");
                                item.activa      = Boolean.TRUE.equals(info.child("activa").getValue(Boolean.class));

                                Long ts = info.child("createdAt").getValue(Long.class);
                                item.fechaCreacion = ts != null ? ts : 0L;

                                // Contar ejercicios
                                item.numEjercicios = (int) ls.child("content/ejercicios")
                                        .getChildrenCount();

                                // Recompensas
                                Long exp = ls.child("rewards/exp").getValue(Long.class);
                                item.exp = exp != null ? exp.intValue() : 30;

                                todasLasLecciones.add(item);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing: " + e.getMessage());
                            }
                        }

                        // Ordenar por fecha descendente
                        todasLasLecciones.sort((a, b) -> Long.compare(b.fechaCreacion, a.fechaCreacion));

                        filtrar(etBuscarLeccion.getText().toString());
                        actualizarContador();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminLeccionesActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getString(DataSnapshot snap, String key, String def) {
        String val = snap.child(key).getValue(String.class);
        return val != null ? val : def;
    }

    // ─────────────────────────────────────────────
    // FILTRAR
    // ─────────────────────────────────────────────
    private void filtrar(String query) {
        leccionesFiltradas.clear();

        for (LeccionItem item : todasLasLecciones) {
            boolean matchQuery = query.isEmpty()
                    || item.titulo.toLowerCase().contains(query.toLowerCase())
                    || item.categoria.toLowerCase().contains(query.toLowerCase());

            boolean matchFiltro = filtroActual.equals("todas")
                    || (filtroActual.equals("activas") && item.activa)
                    || (filtroActual.equals("inactivas") && !item.activa)
                    || filtroActual.equalsIgnoreCase(item.nivel)
                    || filtroActual.equalsIgnoreCase(item.categoria);

            if (matchQuery && matchFiltro) {
                leccionesFiltradas.add(item);
            }
        }

        adapter.notifyDataSetChanged();
        layoutVacio.setVisibility(leccionesFiltradas.isEmpty() ? View.VISIBLE : View.GONE);
        rvLecciones.setVisibility(leccionesFiltradas.isEmpty() ? View.GONE : View.VISIBLE);
        actualizarContador();
    }

    private void actualizarContador() {
        runOnUiThread(() ->
                tvContadorLecciones.setText(
                        leccionesFiltradas.size() + " / " + todasLasLecciones.size() + " lecciones"
                )
        );
    }

    // ─────────────────────────────────────────────
    // ACCIONES SOBRE UNA LECCIÓN
    // ─────────────────────────────────────────────
    public void toggleActivarLeccion(LeccionItem item) {
        boolean nuevoEstado = !item.activa;
        dbRef.child("lessons").child(item.id)
                .child("lessonInfo/activa").setValue(nuevoEstado)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                nuevoEstado ? "✅ Lección activada" : "⏸ Lección desactivada",
                                Toast.LENGTH_SHORT).show()
                );
    }

    public void confirmarEliminar(LeccionItem item, int pos) {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar lección?")
                .setMessage("\"" + item.titulo + "\" se eliminará permanentemente.")
                .setPositiveButton("Eliminar", (d, w) -> eliminarLeccion(item, pos))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarLeccion(LeccionItem item, int pos) {
        dbRef.child("lessons").child(item.id).removeValue()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Lección eliminada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                );
    }

    // ─────────────────────────────────────────────
    // MODELO DE DATOS
    // ─────────────────────────────────────────────
    public static class LeccionItem {
        public String id;
        public String titulo;
        public String descripcion;
        public String nivel;
        public String categoria;
        public boolean activa;
        public long fechaCreacion;
        public int numEjercicios;
        public int exp;
    }

    // ─────────────────────────────────────────────
    // ADAPTER
    // ─────────────────────────────────────────────
    public static class LeccionAdapter
            extends RecyclerView.Adapter<LeccionAdapter.VH> {

        private final List<LeccionItem>  lista;
        private final AdminLeccionesActivity ctx;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        LeccionAdapter(List<LeccionItem> lista, AdminLeccionesActivity ctx) {
            this.lista = lista;
            this.ctx   = ctx;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leccion_docente, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            AdminLeccionesActivity.LeccionItem item = lista.get(pos);

            h.tvTitulo.setText(item.titulo);
            h.tvDescripcion.setText(item.descripcion.isEmpty() ? "Sin descripción" : item.descripcion);
            h.tvNivel.setText(item.nivel);
            h.tvCategoria.setText(item.categoria);
            h.tvEjercicios.setText(item.numEjercicios + " ejercicios • " + item.exp + " EXP");
            h.tvFecha.setText(item.fechaCreacion > 0
                    ? sdf.format(new Date(item.fechaCreacion)) : "—");

            // Estado activa/inactiva
            h.switchActiva.setChecked(item.activa);
            h.tvEstado.setText(item.activa ? "✅ Activa" : "⏸ Inactiva");
            h.tvEstado.setTextColor(item.activa
                    ? Color.parseColor("#2ECC71")
                    : Color.parseColor("#95A5A6"));

            // Color del nivel
            int nivelColor;
            switch (item.nivel) {
                case "Intermedio": nivelColor = Color.parseColor("#F39C12"); break;
                case "Avanzado":   nivelColor = Color.parseColor("#E74C3C"); break;
                default:           nivelColor = Color.parseColor("#27AE60"); break;
            }
            h.tvNivel.setBackgroundColor(nivelColor);

            // Switch toggle activa/inactiva
            h.switchActiva.setOnCheckedChangeListener(null);
            h.switchActiva.setOnCheckedChangeListener((btn, isChecked) -> {
                item.activa = isChecked;
                h.tvEstado.setText(isChecked ? "✅ Activa" : "⏸ Inactiva");
                h.tvEstado.setTextColor(isChecked
                        ? Color.parseColor("#2ECC71")
                        : Color.parseColor("#95A5A6"));
                ctx.toggleActivarLeccion(item);
            });

            // Eliminar
            h.btnEliminar.setOnClickListener(v -> ctx.confirmarEliminar(item, pos));

            // Ver detalles / editar (futuro)
            h.cardLeccion.setOnClickListener(v -> {
                Toast.makeText(ctx, "Vista previa: " + item.titulo, Toast.LENGTH_SHORT).show();
            });

            // Animación entrada
            h.itemView.setAlpha(0f);
            h.itemView.animate().alpha(1f)
                    .setDuration(300)
                    .setStartDelay(pos * 50L)
                    .start();
        }

        @Override
        public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            CardView  cardLeccion;
            TextView  tvTitulo, tvDescripcion, tvNivel, tvCategoria,
                    tvEjercicios, tvFecha, tvEstado;
            // 🔹 CAMBIO: Ahora es SwitchCompat en lugar de Switch
            SwitchCompat switchActiva;
            ImageView btnEliminar;

            VH(View v) {
                super(v);
                cardLeccion   = v.findViewById(R.id.cardLeccionDocente);
                tvTitulo      = v.findViewById(R.id.tvTituloLeccion);
                tvDescripcion = v.findViewById(R.id.tvDescripcionLeccion);
                tvNivel       = v.findViewById(R.id.tvNivelLeccion);
                tvCategoria   = v.findViewById(R.id.tvCategoriaLeccion);
                tvEjercicios  = v.findViewById(R.id.tvEjerciciosLeccion);
                tvFecha       = v.findViewById(R.id.tvFechaLeccion);
                tvEstado      = v.findViewById(R.id.tvEstadoLeccion);
                // 🔹 CAMBIO: SwitchCompat
                switchActiva  = v.findViewById(R.id.switchActivaLeccion);
                btnEliminar   = v.findViewById(R.id.btnEliminarLeccion);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLecciones();
    }
}