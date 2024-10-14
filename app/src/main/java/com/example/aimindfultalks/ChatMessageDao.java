package com.example.aimindfultalks;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    void insertMessage(ChatMessageEntity chatMessage);

    @Query("SELECT * FROM chat_message")
    List<ChatMessageEntity> getAllMessages();

    @Query("DELETE FROM chat_message")
    void clearMessages();
}
