package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class profileActivity extends AppCompatActivity {

    private ListView lvDatos;
    private ArrayAdapter<String> adapter;
    private List<String> listaUsuarios = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    Button btnCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Configurar vistas
        lvDatos = findViewById(R.id.lvDatos);
        Button btnAgregarCuenta = findViewById(R.id.btnAgregarCuenta);

        // Configurar adaptador
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaUsuarios);
        lvDatos.setAdapter(adapter);


        //otro boton
        btnCuenta = findViewById(R.id.btnCuenta);
        btnCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(profileActivity.this, loginFormActivity.class));

            }
        });

        // Listeners
        btnAgregarCuenta.setOnClickListener(v -> {
            startActivity(new Intent(profileActivity.this, loginFormActivity.class));
        });

        lvDatos.setOnItemClickListener((parent, view, position, id) -> {
            String usuarioSeleccionado = listaUsuarios.get(position);
            // Aquí puedes implementar la lógica para iniciar sesión con el usuario seleccionado
            Toast.makeText(this, "Seleccionado: " + usuarioSeleccionado, Toast.LENGTH_SHORT).show();
        });
    }


}