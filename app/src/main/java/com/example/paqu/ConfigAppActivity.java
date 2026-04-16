package com.example.paqu;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ConfigAppActivity extends AppCompatActivity {

    private Switch switchMantenimiento, switchRegistroAbierto;
    private TextInputEditText etMensajeMantenimiento, etVersionApp;
    private DatabaseReference configRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_app2);

        initViews();
        cargarConfiguracion();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        switchMantenimiento = findViewById(R.id.switchMantenimiento);
        switchRegistroAbierto = findViewById(R.id.switchRegistroAbierto);
        etMensajeMantenimiento = findViewById(R.id.etMensajeMantenimiento);
        etVersionApp = findViewById(R.id.etVersionApp);

        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardarConfiguracion());

        MaterialButton btnLimpiarCache = findViewById(R.id.btnLimpiarCache);
        btnLimpiarCache.setOnClickListener(v -> {
            Toast.makeText(this, "Caché limpiado", Toast.LENGTH_SHORT).show();
        });

        configRef = FirebaseDatabase.getInstance().getReference("app_config");
    }

    private void cargarConfiguracion() {
        configRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Boolean mantenimiento = snapshot.child("maintenance").getValue(Boolean.class);
                Boolean registro = snapshot.child("openRegistration").getValue(Boolean.class);
                String mensaje = snapshot.child("maintenanceMessage").getValue(String.class);
                String version = snapshot.child("version").getValue(String.class);

                switchMantenimiento.setChecked(mantenimiento != null ? mantenimiento : false);
                switchRegistroAbierto.setChecked(registro != null ? registro : true);

                if (mensaje != null) etMensajeMantenimiento.setText(mensaje);
                if (version != null) etVersionApp.setText(version);
            }
        });
    }

    private void guardarConfiguracion() {
        Map<String, Object> config = new HashMap<>();
        config.put("maintenance", switchMantenimiento.isChecked());
        config.put("openRegistration", switchRegistroAbierto.isChecked());
        config.put("maintenanceMessage", etMensajeMantenimiento.getText().toString());
        config.put("version", etVersionApp.getText().toString());
        config.put("updatedAt", System.currentTimeMillis());

        configRef.setValue(config)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}