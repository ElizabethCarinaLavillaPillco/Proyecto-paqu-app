package com.example.paqu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TamanioFuenteActivity extends AppCompatActivity {

    private SeekBar seekBarFuente;
    private TextView tvTextoEjemplo;
    private TextView tvTamanioActual;
    private TextView btnAplicar;
    private TextView btnCancelar;

    private final float[] tamaniosFuente = {0.8f, 1.0f, 1.2f, 1.4f, 1.6f};
    private final String[] nombresTamanios = {"Muy Pequeño", "Pequeño", "Mediano", "Grande", "Muy Grande"};
    private int tamanioSeleccionado = 2; // Por defecto mediano

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tamanio_fuente);

        inicializarVistas();
        configurarSeekBar();
        configurarBotones();
        cargarConfiguracionActual();
    }

    private void inicializarVistas() {
        seekBarFuente = findViewById(R.id.seekBarFuente1);
        tvTextoEjemplo = findViewById(R.id.tvTextoEjemplo1);
        tvTamanioActual = findViewById(R.id.tvTamanioActual1);
        btnAplicar = findViewById(R.id.btnAplicar1);
        btnCancelar = findViewById(R.id.btnCancelar1);

        // Configurar SeekBar
        seekBarFuente.setMax(tamaniosFuente.length - 1);
        seekBarFuente.setProgress(tamanioSeleccionado);
    }

    private void configurarSeekBar() {
        seekBarFuente.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tamanioSeleccionado = progress;
                aplicarTamanioEjemplo(progress);
                actualizarTextoIndicador(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Animación o feedback al empezar a deslizar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Feedback al terminar de deslizar
                Toast.makeText(TamanioFuenteActivity.this,
                        nombresTamanios[tamanioSeleccionado] + " seleccionado",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aplicarTamanioEjemplo(int progreso) {
        float factor = tamaniosFuente[progreso];
        float tamanioBase = 16f; // Tamaño base en sp
        float nuevoTamanio = tamanioBase * factor;

        tvTextoEjemplo.setTextSize(TypedValue.COMPLEX_UNIT_SP, nuevoTamanio);
    }

    private void actualizarTextoIndicador(int progreso) {
        String texto = "Tamaño: " + nombresTamanios[progreso];
        tvTamanioActual.setText(texto);
    }

    private void configurarBotones() {
        // Botón Aplicar
        btnAplicar.setOnClickListener(v -> {
            aplicarTamanioGlobal();
            mostrarMensajeExito();
            setResult(RESULT_OK);
            finish();
        });

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void aplicarTamanioGlobal() {
        SharedPreferences prefs = getSharedPreferences("configuracion_app", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("factor_fuente", tamaniosFuente[tamanioSeleccionado]);
        editor.putInt("tamanio_seleccionado", tamanioSeleccionado);
        editor.apply();

        // NUEVO: Enviar broadcast para notificar a toda la app
        Intent intent = new Intent("ACTUALIZAR_FUENTE");
        sendBroadcast(intent);

        mostrarMensajeExito();
        setResult(RESULT_OK);
        finish();
    }

    private void notificarCambioFuente() {
        // Aquí puedes usar EventBus, LiveData, o un Broadcast para notificar a otras actividades
        // Ejemplo básico con Broadcast:
        /*
        Intent intent = new Intent("FUENTE_CAMBIADA");
        intent.putExtra("factor_fuente", tamaniosFuente[tamanioSeleccionado]);
        sendBroadcast(intent);
        */
    }

    private void cargarConfiguracionActual() {
        SharedPreferences prefs = getSharedPreferences("configuracion_app", MODE_PRIVATE);
        float factorGuardado = prefs.getFloat("factor_fuente", 1.0f);
        int tamanioGuardado = prefs.getInt("tamanio_seleccionado", 2);

        // Buscar el índice que corresponde al factor guardado
        for (int i = 0; i < tamaniosFuente.length; i++) {
            if (tamaniosFuente[i] == factorGuardado) {
                tamanioSeleccionado = i;
                break;
            }
        }

        seekBarFuente.setProgress(tamanioSeleccionado);
        aplicarTamanioEjemplo(tamanioSeleccionado);
        actualizarTextoIndicador(tamanioSeleccionado);
    }

    private void mostrarMensajeExito() {
        Toast.makeText(this,
                "Tamaño de fuente aplicado: " + nombresTamanios[tamanioSeleccionado],
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}