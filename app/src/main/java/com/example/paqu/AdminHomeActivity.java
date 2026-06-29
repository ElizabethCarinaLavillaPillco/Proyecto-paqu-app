package com.example.paqu;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = "AdminHome";

    // ── Header ──
    private TextView tvAdminNombre, tvAdminEmail;
    private TextView tvStatUsuarios, tvStatDocentes,
            tvStatLecciones, tvStatPalabras;

    // ── Panel de usuarios ──
    private EditText      etBuscarUsuario;
    private Spinner       spinnerFiltroRol;
    private RecyclerView  rvUsuarios;
    private TextView      tvContadorUsuarios;
    private LinearLayout  layoutCargandoUsuarios;

    // ── Acciones rápidas ──
    private CardView cardVerUsuarios, cardAsignarRoles,
            cardVerLecciones, cardVerPalabras;

    // ── Firebase ──
    private DatabaseReference dbRef;
    private String adminUid;

    // ── Datos ──
    private List<UsuarioItem>  todosUsuarios   = new ArrayList<>();
    private List<UsuarioItem>  usuariosFiltrados = new ArrayList<>();
    private UsuariosAdapter    adapter;
    private String             filtroRolActual = "todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }
        adminUid = user.getUid();
        dbRef    = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupSpinnerFiltro();
        setupRecycler();
        setupListeners();
        cargarStatsHeader();
        cargarUsuarios();
        animarEntrada();
    }

    // ══════════════════════════════════════════════════
    // INIT
    // ══════════════════════════════════════════════════
    private void initViews() {
        tvAdminNombre   = findViewById(R.id.tvAdminNombre);
        tvAdminEmail    = findViewById(R.id.tvAdminEmail);
        tvStatUsuarios  = findViewById(R.id.tvStatUsuarios);
        tvStatDocentes  = findViewById(R.id.tvStatDocentes);
        tvStatLecciones = findViewById(R.id.tvStatLecciones);
        tvStatPalabras  = findViewById(R.id.tvStatPalabras);

        etBuscarUsuario      = findViewById(R.id.etBuscarUsuario);
        spinnerFiltroRol     = findViewById(R.id.spinnerFiltroRolAdmin);
        rvUsuarios           = findViewById(R.id.rvUsuariosAdmin);
        tvContadorUsuarios   = findViewById(R.id.tvContadorUsuariosAdmin);
        layoutCargandoUsuarios = findViewById(R.id.layoutCargandoUsuarios);

        cardVerUsuarios   = findViewById(R.id.cardAdminUsuarios);
        cardAsignarRoles  = findViewById(R.id.cardAdminRoles);
        cardVerLecciones  = findViewById(R.id.cardAdminLecciones);
        cardVerPalabras   = findViewById(R.id.cardAdminPalabras);

        // 🔹 CAMBIO: "users" y campos "name" y "email"
        dbRef.child("users").child(adminUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String nombre = snap.child("name").getValue(String.class);
                        String email  = snap.child("email").getValue(String.class);
                        tvAdminNombre.setText("👑 " + (nombre != null ? nombre : "Administrador"));
                        tvAdminEmail.setText(email != null ? email
                                : FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        Log.e(TAG, "Error cargando datos del admin: " + e.getMessage());
                    }
                });
    }

    // ══════════════════════════════════════════════════
    // SPINNER FILTRO ROL
    // ══════════════════════════════════════════════════
    private void setupSpinnerFiltro() {
        String[] opciones = {"Todos los roles", "Estudiantes", "Docentes", "Administradores"};
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroRol.setAdapter(ad);
        spinnerFiltroRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> p) {}
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                switch (pos) {
                    case 0: filtroRolActual = "todos";         break;
                    case 1: filtroRolActual = "usuario_comun"; break;
                    case 2: filtroRolActual = "docente";       break;
                    case 3: filtroRolActual = "administrador"; break;
                }
                filtrarUsuarios(etBuscarUsuario.getText().toString());
            }
        });
    }

    // ══════════════════════════════════════════════════
    // RECYCLER
    // ══════════════════════════════════════════════════
    private void setupRecycler() {
        adapter = new UsuariosAdapter(usuariosFiltrados, this);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setAdapter(adapter);
        rvUsuarios.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    // ══════════════════════════════════════════════════
    // LISTENERS
    // ══════════════════════════════════════════════════
    private void setupListeners() {
        ImageView btnLogout = findViewById(R.id.btnLogoutAdmin);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> cerrarSesion());

        FloatingActionButton fabCrearUsuario = findViewById(R.id.fabCrearUsuario);
        if (fabCrearUsuario != null) {
            fabCrearUsuario.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminCrearUsuarioActivity.class));
            });
        }

        etBuscarUsuario.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int b, int c) {
                filtrarUsuarios(s.toString());
            }
        });

        cardVerUsuarios.setOnClickListener(v -> {
            animarClick(v);
            scrollAPanelUsuarios();
        });
        cardAsignarRoles.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, AsignarRolesActivity.class));
        });
        cardVerLecciones.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, AdminLeccionesActivity.class));
        });
        cardVerPalabras.setOnClickListener(v -> {
            animarClick(v);
            startActivity(new Intent(this, GestionPalabrasDocenteActivity.class));
        });
    }

    private void scrollAPanelUsuarios() {
        View panelUsuarios = findViewById(R.id.panelGestionUsuarios);
        if (panelUsuarios != null) {
            ScrollView scroll = findViewById(R.id.scrollAdmin);
            if (scroll != null) scroll.smoothScrollTo(0, panelUsuarios.getTop());
        }
    }

    // ══════════════════════════════════════════════════
    // CARGAR USUARIOS DESDE FIREBASE
    // ══════════════════════════════════════════════════
    private void cargarUsuarios() {
        layoutCargandoUsuarios.setVisibility(View.VISIBLE);
        rvUsuarios.setVisibility(View.GONE);

        dbRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "✅ Total usuarios encontrados: " + snapshot.getChildrenCount());

                todosUsuarios.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UsuarioItem u = new UsuarioItem();
                    u.uid = ds.getKey();

                    // 🔹 CAMBIO: Leer campos correctos de la BD
                    u.nombre = ds.child("name").getValue(String.class);
                    u.email = ds.child("email").getValue(String.class);
                    u.rol = ds.child("role").getValue(String.class);

                    Long ts = ds.child("createdAt").getValue(Long.class);
                    u.creadoEn = ts != null ? ts : 0L;

                    if (u.nombre == null) u.nombre = "Sin nombre";
                    if (u.email == null) u.email = "Sin email";
                    if (u.rol == null) u.rol = "usuario_comun";

                    Log.d(TAG, "Usuario: " + u.nombre + " | Email: " + u.email + " | Rol: " + u.rol);

                    if (!u.uid.equals(adminUid)) {
                        todosUsuarios.add(u);
                    }
                }

                todosUsuarios.sort((a, b) -> prioRol(a.rol) - prioRol(b.rol));

                layoutCargandoUsuarios.setVisibility(View.GONE);
                rvUsuarios.setVisibility(View.VISIBLE);
                filtrarUsuarios(etBuscarUsuario.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al cargar usuarios: " + error.getMessage());
                layoutCargandoUsuarios.setVisibility(View.GONE);
                Toast.makeText(AdminHomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ══════════════════════════════════════════════════
    // STATS DEL HEADER (tiempo real)
    // ══════════════════════════════════════════════════
    private void cargarStatsHeader() {
        // 🔹 CAMBIO: "users" → "Usuarios"
        dbRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                long total = snap.getChildrenCount();
                long docentes = 0, admins = 0;
                for (DataSnapshot u : snap.getChildren()) {
                    String rol = u.child("role").getValue(String.class);
                    if (rol == null) rol = u.child("Role").getValue(String.class);
                    if ("docente".equals(rol)) docentes++;
                    if ("administrador".equals(rol)) admins++;
                }
                long estudiantes = total - docentes - admins;
                animarContador(tvStatUsuarios, estudiantes);
                animarContador(tvStatDocentes, docentes);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        dbRef.child("lessons").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                animarContador(tvStatLecciones, snap.getChildrenCount());
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        dbRef.child("diccionario").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                animarContador(tvStatPalabras, snap.getChildrenCount());
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private int prioRol(String rol) {
        if ("administrador".equals(rol)) return 0;
        if ("docente".equals(rol))       return 1;
        return 2;
    }

    // ══════════════════════════════════════════════════
    // FILTRAR USUARIOS
    // ══════════════════════════════════════════════════
    private void filtrarUsuarios(String query) {
        usuariosFiltrados.clear();
        for (UsuarioItem u : todosUsuarios) {
            boolean matchQ = query.isEmpty()
                    || u.nombre.toLowerCase().contains(query.toLowerCase())
                    || u.email.toLowerCase().contains(query.toLowerCase());
            boolean matchR = filtroRolActual.equals("todos")
                    || u.rol.equals(filtroRolActual);
            if (matchQ && matchR) usuariosFiltrados.add(u);
        }
        adapter.notifyDataSetChanged();
        tvContadorUsuarios.setText(usuariosFiltrados.size() + " usuario(s)");
    }

    // ══════════════════════════════════════════════════
    // ACCIONES SOBRE UN USUARIO
    // ══════════════════════════════════════════════════

    public void mostrarDialogoCambiarRol(UsuarioItem u) {
        String[] roles      = {"usuario_comun", "docente", "administrador"};
        String[] rolesLabel = {"👤 Estudiante", "📚 Docente", "👑 Administrador"};

        int selActual = 0;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(u.rol)) { selActual = i; break; }
        }
        final int[] seleccion = {selActual};

        new AlertDialog.Builder(this)
                .setTitle("Cambiar rol de " + u.nombre)
                .setSingleChoiceItems(rolesLabel, selActual, (d, which) -> seleccion[0] = which)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevoRol = roles[seleccion[0]];
                    if (nuevoRol.equals(u.rol)) return;
                    cambiarRol(u, nuevoRol);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cambiarRol(UsuarioItem u, String nuevoRol) {
        dbRef.child("users").child(u.uid).child("role").setValue(nuevoRol)
                .addOnSuccessListener(a -> {
                    String label = etiquetaRol(nuevoRol);
                    Toast.makeText(this, "✅ " + u.nombre + " → " + label, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    public void confirmarEliminarUsuario(UsuarioItem u) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Eliminar usuario")
                .setMessage("¿Eliminar a \"" + u.nombre + "\" (" + u.email + ") de la base de datos?\n\n"
                        + "Nota: su cuenta de autenticación permanecerá activa.")
                .setPositiveButton("Eliminar", (d, w) ->
                        dbRef.child("users").child(u.uid).removeValue()
                                .addOnSuccessListener(a ->
                                        Toast.makeText(this, "Usuario eliminado de la BD", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                )
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public void verDetalleUsuario(UsuarioItem u) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fecha = u.creadoEn > 0 ? sdf.format(new Date(u.creadoEn)) : "Desconocida";

        new AlertDialog.Builder(this)
                .setTitle("👤 " + u.nombre)
                .setMessage(
                        "Email: "    + u.email  + "\n"
                                + "Rol: "      + etiquetaRol(u.rol) + "\n"
                                + "UID: "      + u.uid    + "\n"
                                + "Creado: "   + fecha
                )
                .setPositiveButton("Cambiar rol", (d, w) -> mostrarDialogoCambiarRol(u))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    // ══════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════
    private String etiquetaRol(String rol) {
        switch (rol) {
            case "administrador": return "👑 Administrador";
            case "docente":       return "📚 Docente";
            default:              return "👤 Estudiante";
        }
    }

    private void animarContador(TextView tv, long valor) {
        runOnUiThread(() -> {
            long step = Math.max(1, valor / 15);
            final long[] cur = {0};
            Runnable r = new Runnable() {
                @Override public void run() {
                    cur[0] = Math.min(cur[0] + step, valor);
                    tv.setText(String.valueOf(cur[0]));
                    if (cur[0] < valor) tv.postDelayed(this, 40);
                }
            };
            tv.post(r);
        });
    }

    private void animarClick(View v) {
        v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(100)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                .start();
    }

    private void animarEntrada() {
        View header = findViewById(R.id.headerAdmin);
        if (header != null) {
            header.setTranslationY(-180f); header.setAlpha(0f);
            header.animate().translationY(0f).alpha(1f)
                    .setDuration(550).setInterpolator(new DecelerateInterpolator()).start();
        }
        int[] cardIds = {R.id.cardAdminUsuarios, R.id.cardAdminRoles,
                R.id.cardAdminLecciones, R.id.cardAdminPalabras};
        for (int i = 0; i < cardIds.length; i++) {
            View c = findViewById(cardIds[i]);
            if (c == null) continue;
            c.setAlpha(0f); c.setTranslationY(70f);
            c.animate().alpha(1f).translationY(0f)
                    .setDuration(450).setStartDelay(200 + i * 80L)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }
        View panel = findViewById(R.id.panelGestionUsuarios);
        if (panel != null) {
            panel.setAlpha(0f);
            panel.animate().alpha(1f).setDuration(600).setStartDelay(500).start();
        }
    }

    private void cerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Salir", (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent i = new Intent(this, loginFormActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .setNegativeButton("Cancelar", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarStatsHeader();
    }

    // ══════════════════════════════════════════════════
    // MODELO DE DATOS
    // ══════════════════════════════════════════════════
    public static class UsuarioItem {
        public String uid, nombre, email, rol;
        public long   creadoEn;
    }

    // ══════════════════════════════════════════════════
    // ADAPTER
    // ══════════════════════════════════════════════════
    public static class UsuariosAdapter
            extends RecyclerView.Adapter<UsuariosAdapter.VH> {

        private final List<UsuarioItem> lista;
        private final AdminHomeActivity ctx;

        UsuariosAdapter(List<UsuarioItem> lista, AdminHomeActivity ctx) {
            this.lista = lista; this.ctx = ctx;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_usuario_admin, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            UsuarioItem u = lista.get(pos);

            h.tvNombre.setText(u.nombre);
            h.tvEmail.setText(u.email);

            switch (u.rol) {
                case "administrador":
                    h.tvRolBadge.setText("👑 Admin");
                    h.tvRolBadge.setBackgroundColor(Color.parseColor("#D32F2F"));
                    h.ivAvatar.setColorFilter(Color.parseColor("#D32F2F"));
                    break;
                case "docente":
                    h.tvRolBadge.setText("📚 Docente");
                    h.tvRolBadge.setBackgroundColor(Color.parseColor("#1565C0"));
                    h.ivAvatar.setColorFilter(Color.parseColor("#1565C0"));
                    break;
                default:
                    h.tvRolBadge.setText("👤 Estudiante");
                    h.tvRolBadge.setBackgroundColor(Color.parseColor("#2E7D32"));
                    h.ivAvatar.setColorFilter(Color.parseColor("#2E7D32"));
            }

            h.tvInicial.setText(u.nombre.isEmpty() ? "?" :
                    String.valueOf(u.nombre.charAt(0)).toUpperCase());

            h.itemView.setOnClickListener(v -> ctx.verDetalleUsuario(u));
            h.btnCambiarRol.setOnClickListener(v -> ctx.mostrarDialogoCambiarRol(u));
            h.btnEliminar.setOnClickListener(v -> ctx.confirmarEliminarUsuario(u));

            h.itemView.setAlpha(0f);
            h.itemView.animate().alpha(1f)
                    .setDuration(280).setStartDelay(pos * 35L).start();
        }

        @Override public int getItemCount() { return lista.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView  tvNombre, tvEmail, tvRolBadge, tvInicial;
            ImageView ivAvatar, btnCambiarRol, btnEliminar;

            VH(View v) {
                super(v);
                tvNombre     = v.findViewById(R.id.tvNombreUsuarioAdmin);
                tvEmail      = v.findViewById(R.id.tvEmailUsuarioAdmin);
                tvRolBadge   = v.findViewById(R.id.tvRolBadgeAdmin);
                tvInicial    = v.findViewById(R.id.tvInicialAvatar);
                ivAvatar     = v.findViewById(R.id.ivAvatarCirculoAdmin);
                btnCambiarRol = v.findViewById(R.id.btnCambiarRolAdmin);
                btnEliminar  = v.findViewById(R.id.btnEliminarUsuarioAdmin);
            }
        }
    }
}