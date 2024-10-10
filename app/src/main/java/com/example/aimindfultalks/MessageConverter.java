package com.example.aimindfultalks;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import com.example.aimindfultalks.ChatMessage;

public class MessageConverter {

    @TypeConverter
    public String fromMessageList(List<ChatMessage> messages) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ChatMessage>>() {}.getType();
        return gson.toJson(messages, type);
    }

    @TypeConverter
    public List<ChatMessage> toMessageList(String messagesJson) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ChatMessage>>() {}.getType();
        return gson.fromJson(messagesJson, type);
    }
}

