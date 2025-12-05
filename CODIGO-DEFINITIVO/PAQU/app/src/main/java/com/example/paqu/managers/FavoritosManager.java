package com.example.paqu.managers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.paqu.models.FavoritoPalabra;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager para gestionar favoritos del usuario
 */
public class FavoritosManager {
    private static final String TAG = "FavoritosManager";
    private DatabaseReference database;
    private FirebaseAuth auth;
    private String userId;

    public FavoritosManager() {
        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    // Interfaces de callback
    public interface FavoritoCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface FavoritosListCallback {
        void onSuccess(List<FavoritoPalabra> favoritos);
        void onError(String error);
    }

    public interface CheckFavoritoCallback {
        void onResult(boolean esFavorito);
    }

    /**
     * Agregar palabra a favoritos
     */
    public void agregarFavorito(String quechua, String espanol, String categoria,
                                String pronunciacion, FavoritoCallback callback) {
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String favoritoId = quechua.replaceAll("[^a-zA-Z0-9]", "_");
        FavoritoPalabra favorito = new FavoritoPalabra(favoritoId, quechua, espanol,
                categoria, pronunciacion);

        DatabaseReference favoritoRef = database.child("users")
                .child(userId)
                .child("favoritos")
                .child(favoritoId);

        favoritoRef.setValue(favorito)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Favorito agregado: " + quechua);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al agregar favorito: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Eliminar palabra de favoritos
     */
    public void eliminarFavorito(String quechua, FavoritoCallback callback) {
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String favoritoId = quechua.replaceAll("[^a-zA-Z0-9]", "_");

        DatabaseReference favoritoRef = database.child("users")
                .child(userId)
                .child("favoritos")
                .child(favoritoId);

        favoritoRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Favorito eliminado: " + quechua);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al eliminar favorito: " + e.getMessage());
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Verificar si una palabra es favorita
     */
    public void esFavorito(String quechua, CheckFavoritoCallback callback) {
        if (userId == null) {
            callback.onResult(false);
            return;
        }

        String favoritoId = quechua.replaceAll("[^a-zA-Z0-9]", "_");

        DatabaseReference favoritoRef = database.child("users")
                .child(userId)
                .child("favoritos")
                .child(favoritoId);

        favoritoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al verificar favorito: " + error.getMessage());
                callback.onResult(false);
            }
        });
    }

    /**
     * Obtener todas las palabras favoritas
     */
    public void obtenerFavoritos(FavoritosListCallback callback) {
        if (userId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        DatabaseReference favoritosRef = database.child("users")
                .child(userId)
                .child("favoritos");

        favoritosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FavoritoPalabra> favoritos = new ArrayList<>();

                for (DataSnapshot favoritoSnapshot : snapshot.getChildren()) {
                    FavoritoPalabra favorito = favoritoSnapshot.getValue(FavoritoPalabra.class);
                    if (favorito != null) {
                        favoritos.add(favorito);
                    }
                }

                Log.d(TAG, "✅ Favoritos cargados: " + favoritos.size());
                callback.onSuccess(favoritos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error al cargar favoritos: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Toggle favorito (agregar si no existe, eliminar si existe)
     */
    public void toggleFavorito(String quechua, String espanol, String categoria,
                               String pronunciacion, FavoritoCallback callback) {
        esFavorito(quechua, esFavorito -> {
            if (esFavorito) {
                eliminarFavorito(quechua, callback);
            } else {
                agregarFavorito(quechua, espanol, categoria, pronunciacion, callback);
            }
        });
    }

    /**
     * Obtener cantidad de favoritos
     */
    public void obtenerCantidadFavoritos(CantidadCallback callback) {
        if (userId == null) {
            callback.onResult(0);
            return;
        }

        DatabaseReference favoritosRef = database.child("users")
                .child(userId)
                .child("favoritos");

        favoritosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult((int) snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(0);
            }
        });
    }

    public interface CantidadCallback {
        void onResult(int cantidad);
    }
}