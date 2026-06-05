package com.example.paqu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.paqu.managers.FavoritosManager;
import com.example.paqu.models.DiccionarioActivity;
import com.example.paqu.models.FavoritoPalabra;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity para mostrar palabras favoritas
 */
public class FavoritosActivity extends AppCompatActivity {

    private RecyclerView rvFavoritos;
    private LinearLayout layoutVacio;
    private LottieAnimationView lottieVacio;
    private TextView tvTotalFavoritos;
    private ImageView btnBack;
    private MaterialButton btnIrDiccionario;

    private FavoritosManager favoritosManager;
    private FavoritosAdapter adapter;
    private List<FavoritoPalabra> favoritosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        initViews();
        initManager();
        setupRecyclerView();
        setupListeners();
        loadFavoritos();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvFavoritos = findViewById(R.id.rvFavoritos);
        layoutVacio = findViewById(R.id.layoutVacio);
        lottieVacio = findViewById(R.id.lottieVacio);
        tvTotalFavoritos = findViewById(R.id.tvTotalFavoritos);
        btnIrDiccionario = findViewById(R.id.btnIrDiccionario);
    }

    private void initManager() {
        favoritosManager = new FavoritosManager();
        favoritosList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new FavoritosAdapter(favoritosList, this, new FavoritosAdapter.OnFavoritoClickListener() {
            @Override
            public void onEliminarClick(FavoritoPalabra favorito) {
                eliminarFavorito(favorito);
            }

            @Override
            public void onAudioClick(FavoritoPalabra favorito) {
                Toast.makeText(FavoritosActivity.this,
                        "🔊 " + favorito.getQuechua(), Toast.LENGTH_SHORT).show();
                // TODO: Implementar TTS
            }
        });

        rvFavoritos.setLayoutManager(new GridLayoutManager(this, 2));
        rvFavoritos.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // ✅ CORREGIDO: Botón IR AL DICCIONARIO
        btnIrDiccionario.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritosActivity.this, DiccionarioActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadFavoritos() {
        favoritosManager.obtenerFavoritos(new FavoritosManager.FavoritosListCallback() {
            @Override
            public void onSuccess(List<FavoritoPalabra> favoritos) {
                runOnUiThread(() -> {
                    favoritosList.clear();
                    favoritosList.addAll(favoritos);
                    adapter.notifyDataSetChanged();

                    // Actualizar contador
                    tvTotalFavoritos.setText(favoritos.size() + " favoritos");

                    // Mostrar/ocultar vista vacía
                    if (favoritos.isEmpty()) {
                        rvFavoritos.setVisibility(View.GONE);
                        layoutVacio.setVisibility(View.VISIBLE);
                        lottieVacio.playAnimation();
                    } else {
                        rvFavoritos.setVisibility(View.VISIBLE);
                        layoutVacio.setVisibility(View.GONE);
                        lottieVacio.pauseAnimation();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(FavoritosActivity.this,
                            "Error al cargar favoritos: " + error, Toast.LENGTH_SHORT).show();

                    // Mostrar vista vacía en caso de error
                    rvFavoritos.setVisibility(View.GONE);
                    layoutVacio.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void eliminarFavorito(FavoritoPalabra favorito) {
        favoritosManager.eliminarFavorito(favorito.getQuechua(),
                new FavoritosManager.FavoritoCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(FavoritosActivity.this,
                                "⭐ Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                        loadFavoritos();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(FavoritosActivity.this,
                                "Error al eliminar: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritos();  // Recargar al volver
    }
}