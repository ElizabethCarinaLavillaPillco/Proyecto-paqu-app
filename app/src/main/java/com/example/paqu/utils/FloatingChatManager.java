package com.example.paqu.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.paqu.ChatbotActivity;
import com.example.paqu.R;

public class FloatingChatManager {

    private static View bubbleView;

    // variables para movimiento
    private static float initialX;
    private static float initialY;
    private static float touchX;
    private static float touchY;

    public static void attach(Activity activity) {

        if (activity == null) return;

        ViewGroup root = activity.findViewById(android.R.id.content);

        if (root == null) return;

        // Evitar duplicados
        if (bubbleView != null) {
            ViewGroup parent = (ViewGroup) bubbleView.getParent();
            if (parent != null) parent.removeView(bubbleView);
        }

        bubbleView = LayoutInflater.from(activity)
                .inflate(R.layout.layout_chat_bubble, root, false);

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.bottomMargin = 120;
        params.rightMargin = 32;

        bubbleView.setLayoutParams(params);

        // =========================
        // CLICK (abrir chatbot)
        // =========================
        bubbleView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ChatbotActivity.class);
            activity.startActivity(intent);
        });

        // =========================
        // DRAG (MOVIMIENTO REAL)
        // =========================
        bubbleView.setOnTouchListener(new View.OnTouchListener() {

            private float initialX;
            private float initialY;
            private float touchX;
            private float touchY;

            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                FrameLayout.LayoutParams layoutParams =
                        (FrameLayout.LayoutParams) bubbleView.getLayoutParams();

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:

                        isMoving = false;

                        initialX = layoutParams.rightMargin;
                        initialY = layoutParams.bottomMargin;

                        touchX = event.getRawX();
                        touchY = event.getRawY();

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        float dx = event.getRawX() - touchX;
                        float dy = event.getRawY() - touchY;

                        // si se mueve lo suficiente → es drag
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMoving = true;
                        }

                        layoutParams.rightMargin = (int) (initialX - dx);
                        layoutParams.bottomMargin = (int) (initialY - dy);

                        bubbleView.setLayoutParams(layoutParams);

                        return true;

                    case MotionEvent.ACTION_UP:

                        // 🔥 SI NO SE MOVIÓ → ES CLICK
                        if (!isMoving) {
                            v.performClick();
                        }

                        return true;
                }

                return false;
            }
        });

        root.addView(bubbleView);
    }

    public static void detach() {
        if (bubbleView != null) {

            ViewGroup parent = (ViewGroup) bubbleView.getParent();

            if (parent != null) {
                parent.removeView(bubbleView);
            }

            bubbleView = null;
        }
    }
}