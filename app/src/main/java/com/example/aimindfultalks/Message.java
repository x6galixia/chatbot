package com.example.aimindfultalks;

public class Message {
    public static final int USER_MESSAGE = 0;
    public static final int BOT_MESSAGE = 1;

    private final String content;
    private final boolean isUser;

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }
}