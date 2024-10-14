package com.example.aimindfultalks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText messageEditText;
    private Button sendButton;
    private Button newSessionButton;
    private Button chatHistoryButton;
    private DrawerLayout drawerLayout;
    private ListView chatHistoryList;
    private List<String> chatHistoryLabels;

    // Firestore instance
    private FirebaseFirestore firestore;

    // Room database
    private ChatDatabase chatDatabase;

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firestore and Room
        firestore = FirebaseFirestore.getInstance();
        chatDatabase = ChatDatabase.getInstance(this);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        newSessionButton = findViewById(R.id.newSessionButton);
        chatHistoryButton = findViewById(R.id.chatHistoryButton);

        // Initialize the chat history ListView
        chatHistoryList = findViewById(R.id.chat_history_list);
        chatHistoryLabels = new ArrayList<>();

        // Load chat history and unsaved messages on startup
        loadChatHistory();
        loadUnsavedMessages();  // Load messages from Room

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                chatMessages.add(new ChatMessage("user", message));
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatMessages.size() - 1);
                messageEditText.setText("");
                sendMessageToChatbot(message);
            }
        });

        newSessionButton.setOnClickListener(v -> {
            saveCurrentSession();  // Save the current session before clearing the chat
            clearSavedMessages();  // Clear unsaved messages from Room
            chatMessages.clear();  // Clear the chat messages
            chatAdapter.notifyDataSetChanged();
        });

        chatHistoryButton.setOnClickListener(v -> {
            // Open or close the drawer
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        chatHistoryList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedChat = chatHistoryLabels.get(position);
            loadChatSession(selectedChat);
            drawerLayout.closeDrawer(GravityCompat.START);  // Correctly close the drawer
        });
    }

    private void loadUnsavedMessages() {
        AsyncTask.execute(() -> {
            List<ChatMessageEntity> savedMessages = chatDatabase.chatMessageDao().getAllMessages();
            runOnUiThread(() -> {
                for (ChatMessageEntity entity : savedMessages) {
                    chatMessages.add(new ChatMessage(entity.getSender(), entity.getContent()));
                }
                chatAdapter.notifyDataSetChanged();
                if (!chatMessages.isEmpty()) {
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            });
        });
    }

    private void saveCurrentSession() {
        String sessionLabel = "Session " + (chatHistoryLabels.size() + 1);
        List<ChatMessage> currentMessages = new ArrayList<>(chatMessages);

        // Prepare the data to be saved in Firestore
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("label", sessionLabel);
        sessionData.put("messages", currentMessages);

        // Save the session to Firestore
        firestore.collection("chat_sessions")
                .add(sessionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ChatActivity.this, "Session saved to Firestore", Toast.LENGTH_SHORT).show();
                    loadChatHistory();  // Reload chat history labels after saving
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error saving session", e));
    }

    private void clearSavedMessages() {
        AsyncTask.execute(() -> chatDatabase.chatMessageDao().clearMessages());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveChatMessages();  // Save chat messages when the activity is paused
    }

    private void saveChatMessages() {
        AsyncTask.execute(() -> {
            chatDatabase.chatMessageDao().clearMessages();  // Clear existing messages
            for (ChatMessage message : chatMessages) {
                ChatMessageEntity entity = new ChatMessageEntity(message.getSender(), message.getContent());
                chatDatabase.chatMessageDao().insertMessage(entity);  // Save current messages
            }
        });
    }

    private void loadChatHistory() {
        firestore.collection("chat_sessions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatHistoryLabels.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String sessionLabel = document.getString("label");
                            chatHistoryLabels.add(sessionLabel);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatHistoryLabels);
                        chatHistoryList.setAdapter(adapter);
                    } else {
                        Log.w(TAG, "Error getting chat history.", task.getException());
                    }
                });
    }

    private void loadChatSession(String sessionLabel) {
        firestore.collection("chat_sessions")
                .whereEqualTo("label", sessionLabel)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) document.get("messages");

                        chatMessages.clear();
                        for (Map<String, Object> messageData : messages) {
                            String sender = (String) messageData.get("sender");
                            String content = (String) messageData.get("content");
                            chatMessages.add(new ChatMessage(sender, content));
                        }

                        chatAdapter.notifyDataSetChanged();
                        if (!chatMessages.isEmpty()) {
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                        }
                    } else {
                        Log.w(TAG, "Error loading session", task.getException());
                    }
                });
    }

    private void sendMessageToChatbot(String message) {
        String json = "{\n" +
                "    \"model\": \"meta-llama/Meta-Llama-3-8B-Instruct\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}],\n" +
                "    \"max_tokens\": 500,\n" +
                "    \"stream\": false\n" +
                "}";

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        String apiKey = "Bearer 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";  // Use your actual API key

        HuggingFaceApi huggingFaceApi = RetrofitClient.getRetrofitInstance().create(HuggingFaceApi.class);
        Call<ResponseBody> call = huggingFaceApi.getChatbotResponse(requestBody, apiKey);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String botResponse = response.body().string();
                        Log.d(TAG, "Raw bot response: " + botResponse);

                        JSONObject jsonResponse = new JSONObject(botResponse);

                        if (jsonResponse.has("choices")) {
                            JSONArray choices = jsonResponse.getJSONArray("choices");

                            if (choices.length() > 0) {
                                JSONObject choice = choices.getJSONObject(0);
                                if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                                    String botMessage = choice.getJSONObject("message").getString("content");
                                    chatMessages.add(new ChatMessage("bot", botMessage));  // Store as ChatMessage
                                    chatAdapter.notifyDataSetChanged();
                                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                                } else {
                                    Log.e(TAG, "No 'message' or 'content' in the choice object");
                                }
                            } else {
                                Log.e(TAG, "Empty 'choices' array");
                            }
                        } else {
                            Log.e(TAG, "No 'choices' array in the response");
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}