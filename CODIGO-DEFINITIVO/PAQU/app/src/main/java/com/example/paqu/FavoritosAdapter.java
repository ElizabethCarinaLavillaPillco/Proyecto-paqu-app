package com.example.paqu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.models.FavoritoPalabra;

import java.util.List;

/**
 * Adapter para mostrar favoritos en grid
 */
public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {

    private List<FavoritoPalabra> favoritos;
    private Context context;
    private OnFavoritoClickListener listener;

    public interface OnFavoritoClickListener {
        void onEliminarClick(FavoritoPalabra favorito);
        void onAudioClick(FavoritoPalabra favorito);
    }

    public FavoritosAdapter(List<FavoritoPalabra> favoritos, Context context,
                            OnFavoritoClickListener listener) {
        this.favoritos = favoritos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoritoPalabra favorito = favoritos.get(position);

        holder.tvQuechua.setText(favorito.getQuechua());
        holder.tvEspanol.setText(favorito.getEspanol());
        holder.tvCategoria.setText(favorito.getCategoria());

        // Color según categoría
        int colorRes = getColorForCategory(favorito.getCategoria());
        holder.cardFavorito.setCardBackgroundColor(
                ContextCompat.getColor(context, colorRes)
        );

        // Listeners
        holder.btnEliminar.setOnClickListener(v -> {
            animarClick(v);
            listener.onEliminarClick(favorito);
        });

        holder.btnAudio.setOnClickListener(v -> {
            animarClick(v);
            listener.onAudioClick(favorito);
        });

        // Animación de entrada
        holder.cardFavorito.setAlpha(0f);
        holder.cardFavorito.setScaleX(0.8f);
        holder.cardFavorito.setScaleY(0.8f);

        holder.cardFavorito.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(position * 50)
                .start();
    }

    @Override
    public int getItemCount() {
        return favoritos.size();
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
                return R.color.gris_claro;
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
        CardView cardFavorito;
        TextView tvQuechua, tvEspanol, tvCategoria;
        ImageView btnAudio, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFavorito = itemView.findViewById(R.id.cardFavorito);
            tvQuechua = itemView.findViewById(R.id.tvQuechua);
            tvEspanol = itemView.findViewById(R.id.tvEspanol);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            btnAudio = itemView.findViewById(R.id.btnAudio);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}