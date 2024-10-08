package com.example.aimindfultalks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_BOT = 1;

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUserMessage() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(chatMessage);
        } else {
            ((BotMessageViewHolder) holder).bind(chatMessage);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView userMessageTextView;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            userMessageTextView = itemView.findViewById(R.id.userMessageTextView);
        }

        void bind(ChatMessage message) {
            userMessageTextView.setText(message.getContent());
        }
    }

    class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView botMessageTextView;

        BotMessageViewHolder(View itemView) {
            super(itemView);
            botMessageTextView = itemView.findViewById(R.id.botMessageTextView);
        }

        void bind(ChatMessage message) {
            botMessageTextView.setText(message.getContent());
        }
    }
}
