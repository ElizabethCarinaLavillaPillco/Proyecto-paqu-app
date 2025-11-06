package com.example.plantillaspaqu;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargar directamente tu activity_main.xml
        setContentView(R.layout.activity_main);
    }
}
