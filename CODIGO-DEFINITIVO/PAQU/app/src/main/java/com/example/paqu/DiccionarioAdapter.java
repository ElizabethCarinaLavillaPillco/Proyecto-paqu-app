package com.example.paqu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.managers.FavoritosManager;
import com.example.paqu.models.DiccionarioActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiccionarioAdapter extends RecyclerView.Adapter<DiccionarioAdapter.ViewHolder> {

    private List<DiccionarioActivity.PalabraDiccionario> palabras;
    private Context context;
    private int lastPosition = -1;
    private FavoritosManager favoritosManager;
    private Set<String> palabrasFavoritas;  // Cache de favoritos

    public DiccionarioAdapter(List<DiccionarioActivity.PalabraDiccionario> palabras, Context context) {
        this.palabras = palabras;
        this.context = context;
        this.favoritosManager = new FavoritosManager();
        this.palabrasFavoritas = new HashSet<>();

        // Cargar favoritos al inicializar
        cargarFavoritos();
    }

    private void cargarFavoritos() {
        favoritosManager.obtenerFavoritos(new FavoritosManager.FavoritosListCallback() {
            @Override
            public void onSuccess(List<com.example.paqu.models.FavoritoPalabra> favoritos) {
                palabrasFavoritas.clear();
                for (com.example.paqu.models.FavoritoPalabra fav : favoritos) {
                    palabrasFavoritas.add(fav.getQuechua());
                }
                notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Manejar error silenciosamente
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_palabra_diccionario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiccionarioActivity.PalabraDiccionario palabra = palabras.get(position);

        holder.tvQuechua.setText(palabra.quechua);
        holder.tvEspanol.setText(palabra.espanol);
        holder.tvPronunciacion.setText("🔊 " + palabra.pronunciacion);
        holder.tvCategoria.setText(palabra.categoria);

        // Color según categoría
        int colorRes = getColorForCategory(palabra.categoria);
        holder.indicadorCategoria.setBackgroundColor(context.getResources().getColor(colorRes));

        // Verificar si es favorito y actualizar estrella
        boolean esFavorito = palabrasFavoritas.contains(palabra.quechua);
        actualizarEstrellaFavorito(holder.btnFavorito, esFavorito);

        // Audio
        holder.btnAudio.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(context, "🔊 " + palabra.quechua, Toast.LENGTH_SHORT).show();
            // TODO: Implementar TTS
        });

        // Favoritos con animación
        holder.btnFavorito.setOnClickListener(v -> {
            animarClick(v);
            toggleFavorito(palabra, holder.btnFavorito);
        });

        // Animación de entrada
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return palabras.size();
    }

    /**
     * Toggle favorito con animación
     */
    private void toggleFavorito(DiccionarioActivity.PalabraDiccionario palabra, ImageView btnFavorito) {
        boolean eraFavorito = palabrasFavoritas.contains(palabra.quechua);

        favoritosManager.toggleFavorito(
                palabra.quechua,
                palabra.espanol,
                palabra.categoria,
                palabra.pronunciacion,
                new FavoritosManager.FavoritoCallback() {
                    @Override
                    public void onSuccess() {
                        // Actualizar cache local
                        if (eraFavorito) {
                            palabrasFavoritas.remove(palabra.quechua);
                            Toast.makeText(context, "⭐ Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                        } else {
                            palabrasFavoritas.add(palabra.quechua);
                            Toast.makeText(context, "⭐ Agregado a favoritos", Toast.LENGTH_SHORT).show();
                            animarEstrella(btnFavorito);
                        }

                        // Actualizar UI
                        actualizarEstrellaFavorito(btnFavorito, !eraFavorito);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Actualizar apariencia de la estrella
     */
    private void actualizarEstrellaFavorito(ImageView btnFavorito, boolean esFavorito) {
        if (esFavorito) {
            btnFavorito.setImageResource(R.drawable.ic_star);
            btnFavorito.setColorFilter(
                    ContextCompat.getColor(context, R.color.amarillo)
            );
        } else {
            btnFavorito.setImageResource(R.drawable.ic_star);
            btnFavorito.setColorFilter(
                    ContextCompat.getColor(context, R.color.gris_claro)
            );
        }
    }

    /**
     * Animación especial para la estrella
     */
    private void animarEstrella(ImageView estrella) {
        estrella.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(200)
                .withEndAction(() -> {
                    estrella.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private int getColorForCategory(String categoria) {
        switch (categoria) {
            case "Saludos":
                return R.color.morado;
            case "Familia":
                return R.color.rosado;
            case "Naturaleza":
                return R.color.verde;
            case "Números":
                return R.color.celeste;
            case "Verbos":
                return R.color.naranja;
            case "Frases":
                return R.color.lila;
            default:
                return R.color.gris;
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private void animarClick(View view) {
        view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardPalabra;
        TextView tvQuechua, tvEspanol, tvPronunciacion, tvCategoria;
        ImageView btnAudio, btnFavorito;
        View indicadorCategoria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPalabra = itemView.findViewById(R.id.cardPalabra);
            tvQuechua = itemView.findViewById(R.id.tvQuechua);
            tvEspanol = itemView.findViewById(R.id.tvEspanol);
            tvPronunciacion = itemView.findViewById(R.id.tvPronunciacion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            btnAudio = itemView.findViewById(R.id.btnAudio);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
            indicadorCategoria = itemView.findViewById(R.id.indicadorCategoria);
        }
    }
}