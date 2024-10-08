package com.example.aimindfultalks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder> {

    private final List<String> chatHistoryList;

    public ChatHistoryAdapter(List<String> chatHistoryList) {
        this.chatHistoryList = chatHistoryList;
    }

    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        String message = chatHistoryList.get(position);
        holder.messageTextView.setText(message);
    }

    @Override
    public int getItemCount() {
        return chatHistoryList.size();
    }

    static class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}