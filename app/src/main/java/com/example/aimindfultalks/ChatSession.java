package com.example.aimindfultalks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;

@Entity(tableName = "chat_sessions")
@TypeConverters(MessageConverter.class)
public class ChatSession {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String sessionLabel;

    private List<ChatMessage> messages;

    // Constructor
    public ChatSession(String sessionLabel, List<ChatMessage> messages) {
        this.sessionLabel = sessionLabel;
        this.messages = messages;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSessionLabel() {
        return sessionLabel;
    }

    public void setSessionLabel(String sessionLabel) {
        this.sessionLabel = sessionLabel;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}