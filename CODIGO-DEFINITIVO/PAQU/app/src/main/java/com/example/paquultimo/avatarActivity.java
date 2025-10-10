package com.example.paquultimo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class avatarActivity extends AppCompatActivity {

    private ViewPager2 viewPagerAvatares;
    private int[] avatares = {
            R.drawable.llamaparaperifl,
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4,
            R.drawable.avatar5
    };
    private int selectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);

        viewPagerAvatares = findViewById(R.id.viewPagerAvatares);
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);
        Button btnSeleccionar = findViewById(R.id.btnSeleccionarAvatar);

        // Configurar el adapter
        AvatarAdapter avatarAdapter = new AvatarAdapter(avatares);
        viewPagerAvatares.setAdapter(avatarAdapter);

        // Conectar los dots indicator
        dotsIndicator.setViewPager2(viewPagerAvatares);

        // Obtener la posición actual cuando cambia de página
        viewPagerAvatares.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectedPosition = position;
            }
        });

        btnSeleccionar.setOnClickListener(v -> {
            int selectedAvatar = avatares[selectedPosition];

            // Guardar en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("selected_avatar", selectedAvatar);
            editor.apply();

            // Devolver resultado a perfilActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_avatar", selectedAvatar);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    // Clase adapter interna - Solo para mostrar, sin funcionalidad de selección
    public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

        private int[] avatares;

        public AvatarAdapter(int[] avatares) {
            this.avatares = avatares;
        }

        @NonNull
        @Override
        public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Crear FrameLayout programáticamente para cada item
            FrameLayout itemLayout = new FrameLayout(parent.getContext());
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            // ImageView para el avatar - MÁS GRANDE
            ImageView imageViewAvatar = new ImageView(parent.getContext());
            imageViewAvatar.setId(View.generateViewId());
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            imageParams.gravity = android.view.Gravity.CENTER;
            imageViewAvatar.setLayoutParams(imageParams);
            imageViewAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
            itemLayout.addView(imageViewAvatar);

            return new AvatarViewHolder(itemLayout, imageViewAvatar);
        }

        @Override
        public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
            holder.bind(avatares[position]);

            // NO agregar onClickListener aquí - solo mostrar
        }

        @Override
        public int getItemCount() {
            return avatares.length;
        }

        class AvatarViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageViewAvatar;

            public AvatarViewHolder(@NonNull View itemView, ImageView imageView) {
                super(itemView);
                this.imageViewAvatar = imageView;
            }

            public void bind(int avatarResId) {
                imageViewAvatar.setImageResource(avatarResId);
            }
        }
    }
}