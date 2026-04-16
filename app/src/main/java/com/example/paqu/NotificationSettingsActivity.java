package com.example.paqu;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.paqu.utils.NotificationScheduler;

import java.util.Calendar;

public class NotificationSettingsActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private CheckBox checkMorning, checkAfternoon, checkEvening;
    private TextView textMorningTime, textAfternoonTime, textEveningTime;
    private Spinner spinnerMessageType;
    private Button btnSaveSettings;
    private CardView cardTimeSettings;
    private TextView textPreviewTitle, textPreviewMessage;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // ✅ PEDIR PERMISOS AL INICIAR
        checkAndRequestNotificationPermission();

        // Inicializar vistas
        initViews();

        // Cargar configuraciones guardadas
        loadSavedSettings();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        switchNotifications = findViewById(R.id.switchNotifications);
        checkMorning = findViewById(R.id.checkMorning);
        checkAfternoon = findViewById(R.id.checkAfternoon);
        checkEvening = findViewById(R.id.checkEvening);
        textMorningTime = findViewById(R.id.textMorningTime);
        textAfternoonTime = findViewById(R.id.textAfternoonTime);
        textEveningTime = findViewById(R.id.textEveningTime);
        spinnerMessageType = findViewById(R.id.spinnerMessageType);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        cardTimeSettings = findViewById(R.id.cardTimeSettings);
        textPreviewTitle = findViewById(R.id.textPreviewTitle);
        textPreviewMessage = findViewById(R.id.textPreviewMessage);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // ✅ ELIMINADO: Código del botón de prueba

        // Configurar Spinner
        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.message_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMessageType.setAdapter(adapter);
    }

    private void loadSavedSettings() {
        // Cargar configuración guardada
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(notificationsEnabled);

        // Cargar horarios
        textMorningTime.setText(sharedPreferences.getString("morning_time", "8:00 AM"));
        textAfternoonTime.setText(sharedPreferences.getString("afternoon_time", "2:00 PM"));
        textEveningTime.setText(sharedPreferences.getString("evening_time", "7:00 PM"));

        // Cargar checkboxes
        checkMorning.setChecked(sharedPreferences.getBoolean("morning_enabled", true));
        checkAfternoon.setChecked(sharedPreferences.getBoolean("afternoon_enabled", false));
        checkEvening.setChecked(sharedPreferences.getBoolean("evening_enabled", true));

        // Cargar tipo de mensaje
        int messageType = sharedPreferences.getInt("message_type", 0);
        spinnerMessageType.setSelection(messageType);

        // Actualizar estado inicial
        updateUIState(notificationsEnabled);
        updatePreview();
    }

    private void setupListeners() {
        // Switch principal
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUIState(isChecked);
            updatePreview();
        });

        // Listeners para seleccionar hora
        textMorningTime.setOnClickListener(v -> showTimePicker(textMorningTime, "morning_time"));
        textAfternoonTime.setOnClickListener(v -> showTimePicker(textAfternoonTime, "afternoon_time"));
        textEveningTime.setOnClickListener(v -> showTimePicker(textEveningTime, "evening_time"));

        // Listeners para checkboxes
        View.OnClickListener checkboxListener = v -> updatePreview();
        checkMorning.setOnClickListener(checkboxListener);
        checkAfternoon.setOnClickListener(checkboxListener);
        checkEvening.setOnClickListener(checkboxListener);

        // Spinner listener
        spinnerMessageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Botón guardar
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void updateUIState(boolean notificationsEnabled) {
        // Habilitar/deshabilitar sección de horarios según el switch
        cardTimeSettings.setAlpha(notificationsEnabled ? 1.0f : 0.5f);
        checkMorning.setEnabled(notificationsEnabled);
        checkAfternoon.setEnabled(notificationsEnabled);
        checkEvening.setEnabled(notificationsEnabled);
        textMorningTime.setEnabled(notificationsEnabled);
        textAfternoonTime.setEnabled(notificationsEnabled);
        textEveningTime.setEnabled(notificationsEnabled);
        spinnerMessageType.setEnabled(notificationsEnabled);
    }

    private void showTimePicker(TextView textView, String timeKey) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    String time = formatTime(hourOfDay, minute1);
                    textView.setText(time);
                    updatePreview();
                },
                hour,
                minute,
                false
        );

        timePickerDialog.show();
    }

    private String formatTime(int hour, int minute) {
        String period = "AM";
        if (hour >= 12) {
            period = "PM";
            if (hour > 12) hour -= 12;
        }
        if (hour == 0) hour = 12;

        return String.format("%d:%02d %s", hour, minute, period);
    }

    private void updatePreview() {
        if (!switchNotifications.isChecked()) {
            textPreviewTitle.setText("Recordatorios desactivados");
            textPreviewMessage.setText("Activa los recordatorios para ver vistas previas");
            return;
        }

        // Generar mensaje de preview basado en la configuración
        String title = "¡No rompas tu racha!";
        String message = "Es hora de practicar quechua";

        int messageType = spinnerMessageType.getSelectedItemPosition();
        switch (messageType) {
            case 0: // Motivacionales y Culturales
                title = "¡Sigue aprendiendo quechua!";
                message = "Cada palabra te acerca a tus raíces 🏔️";
                break;
            case 1: // Solo Motivacionales
                title = "¡Tú puedes! 💪";
                message = "Hoy es un gran día para aprender";
                break;
            case 2: // Solo Progreso
                title = "Progreso diario";
                message = "Completa tu lección de hoy";
                break;
        }

        textPreviewTitle.setText(title);
        textPreviewMessage.setText(message);
    }

    private void saveSettings() {
        // ✅ PRIMERO VERIFICAR PERMISOS
        checkAndRequestNotificationPermission();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Guardar configuración
        editor.putBoolean("notifications_enabled", switchNotifications.isChecked());
        editor.putString("morning_time", textMorningTime.getText().toString());
        editor.putString("afternoon_time", textAfternoonTime.getText().toString());
        editor.putString("evening_time", textEveningTime.getText().toString());
        editor.putBoolean("morning_enabled", checkMorning.isChecked());
        editor.putBoolean("afternoon_enabled", checkAfternoon.isChecked());
        editor.putBoolean("evening_enabled", checkEvening.isChecked());
        editor.putInt("message_type", spinnerMessageType.getSelectedItemPosition());

        if (editor.commit()) {
            Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();

            // ✅ PROGRAMAR NOTIFICACIONES REALES
            NotificationScheduler.scheduleDailyNotifications(this);

            // ✅ PRUEBA AUTOMÁTICA después de 2 segundos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                NotificationScheduler.testNotificationNow(this);
                Toast.makeText(this, "🔔 Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
            }, 2000);

        } else {
            Toast.makeText(this, "❌ Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ MÉTODO DE PERMISOS
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Mostrar explicación antes de pedir permiso
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this, "Las notificaciones te recordarán practicar quechua", Toast.LENGTH_LONG).show();
                }

                // Pedir permiso
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    // ✅ MANEJAR RESPUESTA DE PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permisos concedidos - Las notificaciones funcionarán", Toast.LENGTH_SHORT).show();
                // Reprogramar notificaciones ahora que tenemos permisos
                NotificationScheduler.scheduleDailyNotifications(this);
            } else {
                Toast.makeText(this, "❌ Sin permisos - Las notificaciones no funcionarán", Toast.LENGTH_LONG).show();
                // Desactivar notificaciones si no hay permisos
                switchNotifications.setChecked(false);
                updateUIState(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}