package com.example.aimindfultalks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_message")
public class ChatMessageEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String sender;
    private String content;

    public ChatMessageEntity(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSender() { return sender; }
    public String getContent() { return content; }
}
