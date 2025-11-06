package com.example.avatar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {


    private Context context;
    private String[] nombres;
    private int[] imagenes;
    private int selectedPosition = -1;

    public AvatarAdapter(Context context, String[] nombres, int[] imagenes) {
        this.context = context;
        this.nombres = nombres;
        this.imagenes = imagenes;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        holder.imgAvatar.setImageResource(imagenes[position]);
        holder.btnName.setText(nombres[position]);

        // Botón seleccionar → abre FelicidadesActivity
        holder.btnSelect.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();

            Intent intent = new Intent(context, FelicidadesActivity.class);
            intent.putExtra("nombre", nombres[position]);
            intent.putExtra("imagen", imagenes[position]);
            context.startActivity(intent);
        });

        // Marcar el avatar seleccionado con un borde especial
        if (selectedPosition == position) {
            holder.imgAvatar.setBackgroundResource(R.drawable.bg_avatar_circle_selected);
        } else {
            holder.imgAvatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        }
    }

    @Override
    public int getItemCount() {
        return nombres.length;
    }

    public static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        Button btnName, btnSelect;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnName = itemView.findViewById(R.id.btnName);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }


}
