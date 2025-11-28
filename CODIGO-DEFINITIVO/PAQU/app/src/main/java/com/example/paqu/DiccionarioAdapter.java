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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DiccionarioAdapter extends RecyclerView.Adapter<DiccionarioAdapter.ViewHolder> {

    private List<DiccionarioActivity.PalabraDiccionario> palabras;
    private Context context;
    private int lastPosition = -1;

    public DiccionarioAdapter(List<DiccionarioActivity.PalabraDiccionario> palabras, Context context) {
        this.palabras = palabras;
        this.context = context;
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

        // Audio
        holder.btnAudio.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(context, "🔊 " + palabra.quechua, Toast.LENGTH_SHORT).show();
            // TODO: Implementar TTS
        });

        // Favoritos
        holder.btnFavorito.setOnClickListener(v -> {
            animarClick(v);
            Toast.makeText(context, "⭐ Agregado a favoritos", Toast.LENGTH_SHORT).show();
        });

        // Animación de entrada
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return palabras.size();
    }

    private int getColorForCategory(String categoria) {
        switch (categoria) {
            case "Saludos":
                return R.color.morado;
            case "Familia":
                return R.color.rosado;
            case "Naturaleza":
                return android.R.color.holo_green_light;
            case "Números":
                return android.R.color.holo_blue_light;
            case "Verbos":
                return android.R.color.holo_orange_light;
            case "Frases":
                return android.R.color.holo_purple;
            default:
                return R.color.grey;
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