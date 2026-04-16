package com.example.paqu.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.R;
import com.example.paqu.models.VarianteQuechua;

import java.util.List;

/**
 * Adapter para lista de variantes del Quechua
 */
public class VariantesListAdapter extends RecyclerView.Adapter<VariantesListAdapter.VarianteViewHolder> {

    private List<VarianteQuechua> variantes;
    private Context context;
    private OnVarianteClickListener listener;
    private int lastPosition = -1;

    public interface OnVarianteClickListener {
        void onVarianteClick(VarianteQuechua variante);
    }

    public VariantesListAdapter(List<VarianteQuechua> variantes, Context context,
                                OnVarianteClickListener listener) {
        this.variantes = variantes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VarianteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_variante_quechua, parent, false);
        return new VarianteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VarianteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VarianteQuechua variante = variantes.get(position);
        holder.bind(variante);

        // Animación de entrada escalonada
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_slide_up);
            animation.setStartOffset(position * 50L);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return variantes.size();
    }

    class VarianteViewHolder extends RecyclerView.ViewHolder {
        CardView cardVariante;
        View colorBar;
        TextView tvNombre;
        TextView tvRegion;
        TextView tvHablantes;
        TextView tvEjemplo;
        View badgePrincipal;

        public VarianteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardVariante = itemView.findViewById(R.id.cardVariante);
            colorBar = itemView.findViewById(R.id.colorBar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            tvHablantes = itemView.findViewById(R.id.tvHablantes);
            tvEjemplo = itemView.findViewById(R.id.tvEjemplo);
            badgePrincipal = itemView.findViewById(R.id.badgePrincipal);
        }

        public void bind(VarianteQuechua variante) {
            tvNombre.setText(variante.getNombre());
            tvRegion.setText("📍 " + variante.getRegion());
            tvHablantes.setText("👥 " + variante.getHablantesFormateado());
            tvEjemplo.setText("💬 " + variante.getEjemploPalabra());

            // Color identificador
            try {
                int color = Color.parseColor(variante.getColor());
                colorBar.setBackgroundColor(color);
            } catch (Exception e) {
                colorBar.setBackgroundColor(context.getResources().getColor(R.color.morado));
            }

            // Badge si es variante principal
            badgePrincipal.setVisibility(variante.isPrincipal() ? View.VISIBLE : View.GONE);

            // Click listener
            cardVariante.setOnClickListener(v -> {
                animarClick(v);
                if (listener != null) {
                    listener.onVarianteClick(variante);
                }
            });
        }

        private void animarClick(View view) {
            view.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }
    }
}