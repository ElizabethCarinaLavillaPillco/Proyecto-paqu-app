package com.example.paqu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_message, parent, false);
            return new UserVH(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot_message, parent, false);
            return new BotVH(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatMessage msg = messages.get(position);

        if (holder instanceof UserVH) {
            ((UserVH) holder).text.setText(msg.getMessage());
        } else {
            ((BotVH) holder).text.setText(msg.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView text;
        UserVH(View v) {
            super(v);
            text = v.findViewById(R.id.txtUser);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView text;
        BotVH(View v) {
            super(v);
            text = v.findViewById(R.id.txtBot);
        }
    }
}