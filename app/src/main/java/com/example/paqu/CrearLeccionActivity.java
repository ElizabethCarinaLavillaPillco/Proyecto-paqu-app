package com.example.paqu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearLeccionActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion, etNivel;
    private LinearLayout containerEjercicios;
    private MaterialButton btnAgregarEjercicio;
    private List<View> ejerciciosViews;
    private DatabaseReference lessonsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_leccion2);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        lessonsRef = FirebaseDatabase.getInstance().getReference("lessons");
        ejerciciosViews = new ArrayList<>();

        initViews();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etNivel = findViewById(R.id.etNivel);
        containerEjercicios = findViewById(R.id.containerEjercicios);
        btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio);

        btnAgregarEjercicio.setOnClickListener(v -> agregarEjercicio());

        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardarLeccion());

        MaterialButton btnPreview = findViewById(R.id.btnPreview);
        btnPreview.setOnClickListener(v -> mostrarVistaPrevia());

        // Agregar primer ejercicio por defecto
        agregarEjercicio();
    }

    private void agregarEjercicio() {
        View ejercicioView = LayoutInflater.from(this)
                .inflate(R.layout.item_ejercicio_editable, containerEjercicios, false);

        TextInputEditText etPregunta = ejercicioView.findViewById(R.id.etPregunta);
        TextInputEditText etRespuesta = ejercicioView.findViewById(R.id.etRespuesta);
        TextInputEditText etOpcion1 = ejercicioView.findViewById(R.id.etOpcion1);
        TextInputEditText etOpcion2 = ejercicioView.findViewById(R.id.etOpcion2);

        ImageButton btnEliminar = ejercicioView.findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(v -> {
            containerEjercicios.removeView(ejercicioView);
            ejerciciosViews.remove(ejercicioView);
        });

        containerEjercicios.addView(ejercicioView);
        ejerciciosViews.add(ejercicioView);
    }

    private void guardarLeccion() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String nivel = etNivel.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Ingrese título");
            return;
        }

        // Generar ID único
        String lessonId = lessonsRef.push().getKey();

        // Info de la lección
        Map<String, Object> lessonInfo = new HashMap<>();
        lessonInfo.put("id", lessonId);
        lessonInfo.put("title", titulo);
        lessonInfo.put("description", descripcion);
        lessonInfo.put("level", nivel.isEmpty() ? "1" : nivel);
        lessonInfo.put("createdBy", userId);
        lessonInfo.put("createdAt", System.currentTimeMillis());
        lessonInfo.put("status", "published"); // o "draft"
        lessonInfo.put("totalExercises", ejerciciosViews.size());

        // Contenido (ejercicios)
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

        // Recompensas
        Map<String, Object> rewards = new HashMap<>();
        rewards.put("xp", 10 * ejerciciosViews.size());
        rewards.put("coins", 5 * ejerciciosViews.size());

        // Estructura final
        Map<String, Object> lessonData = new HashMap<>();
        lessonData.put("lessonInfo", lessonInfo);
        lessonData.put("content", exercises);
        lessonData.put("rewards", rewards);

        lessonsRef.child(lessonId).setValue(lessonData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lección creada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarVistaPrevia() {
        new AlertDialog.Builder(this)
                .setTitle("Vista Previa")
                .setMessage("Título: " + etTitulo.getText() +
                        "\nEjercicios: " + ejerciciosViews.size())
                .setPositiveButton("OK", null)
                .show();
    }
}