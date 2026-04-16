package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MisLeccionesActivity extends AppCompatActivity {

    private RecyclerView recyclerLecciones;
    private LeccionesAdapter adapter;
    private List<LeccionItem> listaLecciones;
    private DatabaseReference lessonsRef;
    private String userId;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_lecciones2);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        lessonsRef = FirebaseDatabase.getInstance().getReference("lessons");

        initViews();
        setupRecycler();
        cargarLecciones();
    }

    private void initViews() {
        recyclerLecciones = findViewById(R.id.recyclerLecciones);
        tvEmpty = findViewById(R.id.tvEmpty);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        FloatingActionButton fabNueva = findViewById(R.id.fabNueva);
        fabNueva.setOnClickListener(v -> {
            startActivity(new Intent(this, CrearLeccionActivity.class));
        });
    }

    private void setupRecycler() {
        listaLecciones = new ArrayList<>();
        adapter = new LeccionesAdapter(listaLecciones, new LeccionesAdapter.OnLeccionClickListener() {
            @Override
            public void onEditClick(LeccionItem leccion) {
                editarLeccion(leccion);
            }

            @Override
            public void onDeleteClick(LeccionItem leccion) {
                confirmarEliminar(leccion);
            }

            @Override
            public void onStatsClick(LeccionItem leccion) {
                verEstadisticas(leccion);
            }
        });

        recyclerLecciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerLecciones.setAdapter(adapter);
    }

    private void cargarLecciones() {
        lessonsRef.orderByChild("lessonInfo/createdBy").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaLecciones.clear();

                        for (DataSnapshot lessonSnap : snapshot.getChildren()) {
                            String id = lessonSnap.getKey();
                            String title = lessonSnap.child("lessonInfo/title").getValue(String.class);
                            String description = lessonSnap.child("lessonInfo/description").getValue(String.class);
                            String level = lessonSnap.child("lessonInfo/level").getValue(String.class);
                            Long createdAt = lessonSnap.child("lessonInfo/createdAt").getValue(Long.class);
                            String status = lessonSnap.child("lessonInfo/status").getValue(String.class);
                            Long totalExercises = lessonSnap.child("lessonInfo/totalExercises").getValue(Long.class);

                            if (title == null) title = "Sin título";

                            listaLecciones.add(new LeccionItem(
                                    id, title, description, level,
                                    createdAt != null ? createdAt : 0,
                                    status != null ? status : "draft",
                                    totalExercises != null ? totalExercises.intValue() : 0
                            ));
                        }

                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(listaLecciones.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MisLeccionesActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editarLeccion(LeccionItem leccion) {
        Intent intent = new Intent(this, EditarLeccionActivity.class);
        intent.putExtra("lessonId", leccion.id);
        startActivity(intent);
    }

    private void confirmarEliminar(LeccionItem leccion) {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar lección?")
                .setMessage("'" + leccion.title + "' se eliminará permanentemente")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    lessonsRef.child(leccion.id).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Lección eliminada", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void verEstadisticas(LeccionItem leccion) {
        // Ver cuántos usuarios completaron la lección
        DatabaseReference userLessonsRef = FirebaseDatabase.getInstance().getReference("user_lessons");
        userLessonsRef.orderByChild("lessonId").equalTo(leccion.id)
                .get().addOnSuccessListener(snapshot -> {
                    int completados = 0;
                    int enProgreso = 0;

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Boolean completed = snap.child("completed").getValue(Boolean.class);
                        if (completed != null && completed) {
                            completados++;
                        } else {
                            enProgreso++;
                        }
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Estadísticas: " + leccion.title)
                            .setMessage("Completados: " + completados +
                                    "\nEn progreso: " + enProgreso +
                                    "\nTotal ejercicios: " + leccion.totalExercises)
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    // ===== CLASES INTERNAS =====

    public static class LeccionItem {
        String id, title, description, level, status;
        long createdAt;
        int totalExercises;

        public LeccionItem(String id, String title, String description, String level,
                           long createdAt, String status, int totalExercises) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.level = level;
            this.createdAt = createdAt;
            this.status = status;
            this.totalExercises = totalExercises;
        }
    }

    public static class LeccionesAdapter extends RecyclerView.Adapter<LeccionesAdapter.ViewHolder> {

        private List<LeccionItem> lecciones;
        private OnLeccionClickListener listener;

        public interface OnLeccionClickListener {
            void onEditClick(LeccionItem leccion);
            void onDeleteClick(LeccionItem leccion);
            void onStatsClick(LeccionItem leccion);
        }

        public LeccionesAdapter(List<LeccionItem> lecciones, OnLeccionClickListener listener) {
            this.lecciones = lecciones;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mis_lecciones, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeccionItem l = lecciones.get(position);
            holder.tvTitulo.setText(l.title);
            holder.tvDescripcion.setText(l.description != null ? l.description : "Sin descripción");
            holder.tvNivel.setText("Nivel: " + l.level);
            holder.tvEjercicios.setText(l.totalExercises + " ejercicios");

            // Estado con color
            if (l.status.equals("published")) {
                holder.tvEstado.setText("Publicada");
                holder.tvEstado.setTextColor(0xFF4CAF50); // Verde
            } else {
                holder.tvEstado.setText("Borrador");
                holder.tvEstado.setTextColor(0xFFFF9800); // Naranja
            }

            holder.btnEditar.setOnClickListener(v -> listener.onEditClick(l));
            holder.btnEliminar.setOnClickListener(v -> listener.onDeleteClick(l));
            holder.btnStats.setOnClickListener(v -> listener.onStatsClick(l));
        }

        @Override
        public int getItemCount() { return lecciones.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvDescripcion, tvNivel, tvEjercicios, tvEstado;
            ImageButton btnEditar, btnEliminar, btnStats;

            public ViewHolder(View itemView) {
                super(itemView);
                tvTitulo = itemView.findViewById(R.id.tvTitulo);
                tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
                tvNivel = itemView.findViewById(R.id.tvNivel);
                tvEjercicios = itemView.findViewById(R.id.tvEjercicios);
                tvEstado = itemView.findViewById(R.id.tvEstado);
                btnEditar = itemView.findViewById(R.id.btnEditar);
                btnEliminar = itemView.findViewById(R.id.btnEliminar);
                btnStats = itemView.findViewById(R.id.btnStats);
            }
        }
    }
}