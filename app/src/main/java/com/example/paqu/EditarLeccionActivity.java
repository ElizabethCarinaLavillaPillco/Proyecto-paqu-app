package com.example.paqu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarLeccionActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion, etNivel;
    private LinearLayout containerEjercicios;
    private DatabaseReference lessonRef;
    private String lessonId;
    private List<View> ejerciciosViews;
    private List<String> ejerciciosIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_leccion); // Reusa el mismo layout

        lessonId = getIntent().getStringExtra("lessonId");
        if (lessonId == null) {
            Toast.makeText(this, "Error: ID de lección no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lessonRef = FirebaseDatabase.getInstance().getReference("lessons").child(lessonId);
        ejerciciosViews = new ArrayList<>();
        ejerciciosIds = new ArrayList<>();

        initViews();
        cargarLeccion();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etNivel = findViewById(R.id.etNivel);
        containerEjercicios = findViewById(R.id.containerEjercicios);

        MaterialButton btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio);
        btnAgregarEjercicio.setOnClickListener(v -> agregarEjercicioVacio());

        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setText("Actualizar"); // Cambiar texto
        btnGuardar.setOnClickListener(v -> actualizarLeccion());

        // Ocultar botón preview si existe
        MaterialButton btnPreview = findViewById(R.id.btnPreview);
        if (btnPreview != null) btnPreview.setVisibility(View.GONE);
    }

    private void cargarLeccion() {
        lessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(EditarLeccionActivity.this,
                            "Lección no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Cargar info básica
                String title = snapshot.child("lessonInfo/title").getValue(String.class);
                String description = snapshot.child("lessonInfo/description").getValue(String.class);
                String level = snapshot.child("lessonInfo/level").getValue(String.class);

                etTitulo.setText(title);
                etDescripcion.setText(description);
                etNivel.setText(level);

                // Cargar ejercicios
                DataSnapshot contentSnap = snapshot.child("content");
                for (DataSnapshot ejercicioSnap : contentSnap.getChildren()) {
                    String ejercicioId = ejercicioSnap.getKey();
                    String question = ejercicioSnap.child("question").getValue(String.class);
                    String correct = ejercicioSnap.child("correctAnswer").getValue(String.class);
                    String option1 = ejercicioSnap.child("option1").getValue(String.class);
                    String option2 = ejercicioSnap.child("option2").getValue(String.class);

                    agregarEjercicioExistente(ejercicioId, question, correct, option1, option2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditarLeccionActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarEjercicioExistente(String id, String question, String correct,
                                           String option1, String option2) {
        View ejercicioView = LayoutInflater.from(this)
                .inflate(R.layout.item_ejercicio_editable, containerEjercicios, false);

        TextInputEditText etPregunta = ejercicioView.findViewById(R.id.etPregunta);
        TextInputEditText etRespuesta = ejercicioView.findViewById(R.id.etRespuesta);
        TextInputEditText etOpcion1 = ejercicioView.findViewById(R.id.etOpcion1);
        TextInputEditText etOpcion2 = ejercicioView.findViewById(R.id.etOpcion2);

        etPregunta.setText(question);
        etRespuesta.setText(correct);
        etOpcion1.setText(option1);
        etOpcion2.setText(option2);

        ImageButton btnEliminar = ejercicioView.findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(v -> {
            // Eliminar de Firebase si tiene ID
            if (id != null && !id.isEmpty()) {
                lessonRef.child("content").child(id).removeValue();
            }
            containerEjercicios.removeView(ejercicioView);
            ejerciciosViews.remove(ejercicioView);
        });

        containerEjercicios.addView(ejercicioView);
        ejerciciosViews.add(ejercicioView);
        ejerciciosIds.add(id);
    }

    private void agregarEjercicioVacio() {
        agregarEjercicioExistente(null, "", "", "", "");
    }

    private void actualizarLeccion() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String nivel = etNivel.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Ingrese título");
            return;
        }

        // Actualizar info
        Map<String, Object> updates = new HashMap<>();
        updates.put("lessonInfo/title", titulo);
        updates.put("lessonInfo/description", descripcion);
        updates.put("lessonInfo/level", nivel);
        updates.put("lessonInfo/updatedAt", System.currentTimeMillis());
        updates.put("lessonInfo/totalExercises", ejerciciosViews.size());

        // Actualizar ejercicios
        List<Map<String, Object>> exercises = new ArrayList<>();
        for (int i = 0; i < ejerciciosViews.size(); i++) {
            View v = ejerciciosViews.get(i);
            TextInputEditText etPregunta = v.findViewById(R.id.etPregunta);
            TextInputEditText etRespuesta = v.findViewById(R.id.etRespuesta);
            TextInputEditText etOpcion1 = v.findViewById(R.id.etOpcion1);
            TextInputEditText etOpcion2 = v.findViewById(R.id.etOpcion2);

            Map<String, Object> ejercicio = new HashMap<>();
            ejercicio.put("question", etPregunta.getText().toString());
            ejercicio.put("correctAnswer", etRespuesta.getText().toString());
            ejercicio.put("option1", etOpcion1.getText().toString());
            ejercicio.put("option2", etOpcion2.getText().toString());
            ejercicio.put("type", "multiple_choice");
            ejercicio.put("order", i);

            exercises.add(ejercicio);
        }

        updates.put("content", exercises);

        // Recompensas actualizadas
        Map<String, Object> rewards = new HashMap<>();
        rewards.put("xp", 10 * ejerciciosViews.size());
        rewards.put("coins", 5 * ejerciciosViews.size());
        updates.put("rewards", rewards);

        lessonRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lección actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}