package com.example.aimindfultalks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

    private List<String> chatLabels;
    private OnChatClickListener onChatClickListener;

    public ChatHistoryAdapter(List<String> chatLabels, OnChatClickListener listener) {
        this.chatLabels = chatLabels;
        this.onChatClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String label = chatLabels.get(position);
        holder.labelTextView.setText(label);
        holder.itemView.setOnClickListener(v -> onChatClickListener.onChatClick(label));
    }

    @Override
    public int getItemCount() {
        return chatLabels.size();
    }

    public interface OnChatClickListener {
        void onChatClick(String chatLabel);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelTextView;

        ViewHolder(View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}