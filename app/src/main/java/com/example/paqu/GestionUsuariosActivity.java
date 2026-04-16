package com.example.paqu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GestionUsuariosActivity extends AppCompatActivity {

    private RecyclerView recyclerUsuarios;
    private UsuariosAdapter adapter;
    private List<UsuarioItem> listaUsuarios;
    private DatabaseReference usersRef;
    private FloatingActionButton fabAgregarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios3);

        initViews();
        setupRecycler();
        cargarUsuarios();
    }

    private void initViews() {
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        fabAgregarUsuario = findViewById(R.id.fabAgregarUsuario);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        fabAgregarUsuario.setOnClickListener(v -> {
            // Ir a crear usuario manualmente
            startActivity(new android.content.Intent(this, CrearUsuarioAdminActivity.class));
        });
    }

    private void setupRecycler() {
        listaUsuarios = new ArrayList<>();
        adapter = new UsuariosAdapter(listaUsuarios, this::onUsuarioClick);
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

                    listaUsuarios.add(new UsuarioItem(uid, nombre, email, rol));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionUsuariosActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onUsuarioClick(UsuarioItem usuario) {
        String[] opciones = {"Ver detalles", "Cambiar rol", "Eliminar usuario", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle(usuario.nombre)
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0: verDetalles(usuario); break;
                        case 1: mostrarDialogoCambiarRol(usuario); break;
                        case 2: confirmarEliminar(usuario); break;
                    }
                })
                .show();
    }

    private void mostrarDialogoCambiarRol(UsuarioItem usuario) {
        String[] roles = {"usuario_comun", "docente", "administrador"};

        new AlertDialog.Builder(this)
                .setTitle("Cambiar rol de " + usuario.nombre)
                .setItems(roles, (dialog, which) -> {
                    String nuevoRol = roles[which];
                    usersRef.child(usuario.uid).child("role").setValue(nuevoRol)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Rol actualizado a " + nuevoRol,
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .show();
    }

    private void confirmarEliminar(UsuarioItem usuario) {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar usuario?")
                .setMessage("Esta acción no se puede deshacer")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Solo elimina de la BD, no del Auth (requiere Cloud Functions para eso)
                    usersRef.child(usuario.uid).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void verDetalles(UsuarioItem usuario) {
        // Mostrar diálogo con info detallada
        new AlertDialog.Builder(this)
                .setTitle(usuario.nombre)
                .setMessage("Email: " + usuario.email +
                        "\nRol: " + usuario.rol +
                        "\nUID: " + usuario.uid)
                .setPositiveButton("OK", null)
                .show();
    }

    // ===== CLASES INTERNAS =====

    public static class UsuarioItem {
        String uid, nombre, email, rol;

        public UsuarioItem(String uid, String nombre, String email, String rol) {
            this.uid = uid;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
        }
    }

    public static class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.ViewHolder> {

        private List<UsuarioItem> usuarios;
        private OnUsuarioClickListener listener;

        public interface OnUsuarioClickListener {
            void onClick(UsuarioItem usuario);
        }

        public UsuariosAdapter(List<UsuarioItem> usuarios, OnUsuarioClickListener listener) {
            this.usuarios = usuarios;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_usuario, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UsuarioItem u = usuarios.get(position);
            holder.tvNombre.setText(u.nombre);
            holder.tvEmail.setText(u.email);
            holder.tvRol.setText(u.rol);

            // Color según rol
            int color;
            switch (u.rol) {
                case "administrador": color = 0xFF9C27B0; break; // Morado
                case "docente": color = 0xFF2196F3; break;      // Azul
                default: color = 0xFF4CAF50; break;             // Verde
            }
            holder.tvRol.setTextColor(color);

            holder.itemView.setOnClickListener(v -> listener.onClick(u));
        }

        @Override
        public int getItemCount() { return usuarios.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvEmail, tvRol;

            public ViewHolder(View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombre);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvRol = itemView.findViewById(R.id.tvRol);
            }
        }
    }
}