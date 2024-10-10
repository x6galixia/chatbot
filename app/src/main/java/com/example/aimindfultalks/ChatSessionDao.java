package com.example.aimindfultalks;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatSessionDao {

    @Insert
    void insertSession(ChatSession session);

    @Query("SELECT * FROM chat_sessions")
    List<ChatSession> getAllSessions();

    @Query("SELECT * FROM chat_sessions WHERE sessionLabel = :label LIMIT 1")
    ChatSession getSessionByLabel(String label);

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    void deleteSessionById(int sessionId);

    @Query("DELETE FROM chat_sessions")
    void deleteAllSessions(); // For deleting all sessions
}