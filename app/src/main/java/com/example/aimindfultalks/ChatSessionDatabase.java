package com.example.aimindfultalks;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChatSession.class}, version = 1)
public abstract class ChatSessionDatabase extends RoomDatabase {
    private static ChatSessionDatabase instance;

    public abstract ChatSessionDao chatSessionDao();

    public static synchronized ChatSessionDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ChatSessionDatabase.class, "chat_session_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}