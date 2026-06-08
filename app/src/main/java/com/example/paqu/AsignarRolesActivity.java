package com.example.paqu;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignarRolesActivity extends AppCompatActivity {

    private Spinner spinnerFiltrarRol;
    private RecyclerView recyclerUsuarios;
    private UsuariosRolAdapter adapter;
    private List<GestionUsuariosActivity.UsuarioItem> listaUsuarios;
    private List<GestionUsuariosActivity.UsuarioItem> listaFiltrada;
    private DatabaseReference usersRef;
    private String filtroActual = "todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_roles2);

        initViews();
        setupSpinner();
        setupRecycler();
        cargarUsuarios();
    }

    private void initViews() {
        spinnerFiltrarRol = findViewById(R.id.spinnerFiltrarRol);
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        MaterialButton btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    private void setupSpinner() {
        String[] opciones = {"Todos", "Usuarios", "Docentes", "Administradores"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltrarRol.setAdapter(adapter);

        spinnerFiltrarRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarLista(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecycler() {
        listaUsuarios = new ArrayList<>();
        listaFiltrada = new ArrayList<>();
        adapter = new UsuariosRolAdapter(listaFiltrada);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsuarios.setAdapter(adapter);
    }

    private void cargarUsuarios() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaUsuarios.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String nombre = userSnap.child("name").getValue(String.class);
                    String email = userSnap.child("email").getValue(String.class);
                    String rol = userSnap.child("role").getValue(String.class);

                    if (nombre == null) nombre = "Sin nombre";
                    if (email == null) email = "Sin email";
                    if (rol == null) rol = "usuario_comun";

                    listaUsuarios.add(new GestionUsuariosActivity.UsuarioItem(uid, nombre, email, rol));
                }

                filtrarLista(spinnerFiltrarRol.getSelectedItemPosition());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AsignarRolesActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarLista(int filtro) {
        listaFiltrada.clear();

        for (GestionUsuariosActivity.UsuarioItem u : listaUsuarios) {
            boolean agregar = false;
            switch (filtro) {
                case 0: agregar = true; break; // Todos
                case 1: agregar = u.rol.equals("usuario_comun"); break;
                case 2: agregar = u.rol.equals("docente"); break;
                case 3: agregar = u.rol.equals("administrador"); break;
            }
            if (agregar) listaFiltrada.add(u);
        }

        adapter.notifyDataSetChanged();
    }

    private void guardarCambios() {
        Map<String, Object> cambios = adapter.getCambios();

        if (cambios.isEmpty()) {
            Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aplicar cambios a Firebase
        for (Map.Entry<String, Object> entry : cambios.entrySet()) {
            usersRef.child(entry.getKey()).child("role").setValue(entry.getValue());
        }

        Toast.makeText(this, "Roles actualizados", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Adapter con selección de rol
    public static class UsuariosRolAdapter
            extends RecyclerView.Adapter<UsuariosRolAdapter.ViewHolder> {

        private List<GestionUsuariosActivity.UsuarioItem> usuarios;
        private Map<String, String> cambiosPendientes = new HashMap<>();

        public UsuariosRolAdapter(List<GestionUsuariosActivity.UsuarioItem> usuarios) {
            this.usuarios = usuarios;
        }

        public Map<String, Object> getCambios() {
            return new HashMap<>(cambiosPendientes);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_usuario_rol, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GestionUsuariosActivity.UsuarioItem u = usuarios.get(position);
            holder.tvNombre.setText(u.nombre);
            holder.tvEmail.setText(u.email);

            // Configurar spinner de rol
            String[] roles = {"usuario_comun", "docente", "administrador"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(),
                    android.R.layout.simple_spinner_item, roles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinnerRol.setAdapter(adapter);

            // Seleccionar rol actual
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(u.rol)) {
                    holder.spinnerRol.setSelection(i);
                    break;
                }
            }

            holder.spinnerRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String nuevoRol = roles[pos];
                    if (!nuevoRol.equals(u.rol)) {
                        cambiosPendientes.put(u.uid, nuevoRol);
                    } else {
                        cambiosPendientes.remove(u.uid);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        @Override
        public int getItemCount() { return usuarios.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvEmail;
            Spinner spinnerRol;

            public ViewHolder(View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombre);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                spinnerRol = itemView.findViewById(R.id.spinnerRol);
            }
        }
    }
}