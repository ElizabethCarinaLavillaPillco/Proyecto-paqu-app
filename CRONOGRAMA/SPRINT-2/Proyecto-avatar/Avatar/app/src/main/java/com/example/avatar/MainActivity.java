package com.example.avatar;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AvatarAdapter adapter;

    private String[] nombres = {"Chinchilla", "Cóndor", "Gato", "Venado", "Vicuña", "Zorro"};
    private int[] imagenes = {
            R.drawable.chinchilla,
            R.drawable.condor,
            R.drawable.gato,
            R.drawable.venado,
            R.drawable.vicuna,
            R.drawable.zorro
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerAvatars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Crear el adapter sin OnAvatarSelectListener
        adapter = new AvatarAdapter(this, nombres, imagenes);

        recyclerView.setAdapter(adapter);
    }

}
