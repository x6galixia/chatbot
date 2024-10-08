package com.example.aimindfultalks;

public class ChatMessage {
    private String sender; // "user" or "bot"
    private String content;

    public ChatMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public boolean isUserMessage() {
        return "user".equals(sender);
    }
}