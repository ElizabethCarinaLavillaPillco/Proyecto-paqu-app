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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class GestionNivelesActivity extends AppCompatActivity {

    private LinearLayout containerNiveles;
    private DatabaseReference levelsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_niveles2);

        levelsRef = FirebaseDatabase.getInstance().getReference("levels");

        initViews();
        cargarNiveles();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        containerNiveles = findViewById(R.id.containerNiveles);

        MaterialButton btnAgregarNivel = findViewById(R.id.btnAgregarNivel);
        btnAgregarNivel.setOnClickListener(v -> mostrarDialogoNuevoNivel());
    }

    private void cargarNiveles() {
        // Cargar niveles existentes de Firebase
        levelsRef.get().addOnSuccessListener(snapshot -> {
            containerNiveles.removeAllViews();

            for (com.google.firebase.database.DataSnapshot levelSnap : snapshot.getChildren()) {
                String id = levelSnap.getKey();
                String name = levelSnap.child("name").getValue(String.class);
                String description = levelSnap.child("description").getValue(String.class);
                Long minXP = levelSnap.child("minXP").getValue(Long.class);
                Long order = levelSnap.child("order").getValue(Long.class);

                agregarVistaNivel(id, name, description,
                        minXP != null ? minXP.intValue() : 0,
                        order != null ? order.intValue() : 0);
            }
        });
    }

    private void agregarVistaNivel(String id, String name, String description, int minXP, int order) {
        View nivelView = LayoutInflater.from(this)
                .inflate(R.layout.item_nivel_editable, containerNiveles, false);

        TextInputEditText etNombre = nivelView.findViewById(R.id.etNombre);
        TextInputEditText etDescripcion = nivelView.findViewById(R.id.etDescripcion);
        TextInputEditText etMinXP = nivelView.findViewById(R.id.etMinXP);
        TextInputEditText etOrden = nivelView.findViewById(R.id.etOrden);

        etNombre.setText(name);
        etDescripcion.setText(description);
        etMinXP.setText(String.valueOf(minXP));
        etOrden.setText(String.valueOf(order));

        ImageButton btnGuardar = nivelView.findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> {
            Map<String, Object> nivelData = new HashMap<>();
            nivelData.put("name", etNombre.getText().toString());
            nivelData.put("description", etDescripcion.getText().toString());
            nivelData.put("minXP", Integer.parseInt(etMinXP.getText().toString()));
            nivelData.put("order", Integer.parseInt(etOrden.getText().toString()));

            levelsRef.child(id).updateChildren(nivelData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Nivel actualizado", Toast.LENGTH_SHORT).show();
                    });
        });

        ImageButton btnEliminar = nivelView.findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("¿Eliminar nivel?")
                    .setMessage("Las lecciones de este nivel quedarán sin categoría")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        levelsRef.child(id).removeValue();
                        containerNiveles.removeView(nivelView);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        containerNiveles.addView(nivelView);
    }

    private void mostrarDialogoNuevoNivel() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_nuevo_nivel, null);

        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombre);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        TextInputEditText etMinXP = dialogView.findViewById(R.id.etMinXP);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Nivel")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String id = levelsRef.push().getKey();

                    Map<String, Object> nivelData = new HashMap<>();
                    nivelData.put("name", etNombre.getText().toString());
                    nivelData.put("description", etDescripcion.getText().toString());
                    nivelData.put("minXP", Integer.parseInt(etMinXP.getText().toString()));
                    nivelData.put("order", containerNiveles.getChildCount());

                    levelsRef.child(id).setValue(nivelData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Nivel creado", Toast.LENGTH_SHORT).show();
                                cargarNiveles(); // Recargar
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}