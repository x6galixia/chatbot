package com.example.aimindfultalks;

import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private static final String SHARED_PREFS = "chat_prefs";
    private static final String CHAT_MESSAGES_KEY = "chat_messages";

    // Room database instance
    private ChatSessionDatabase chatSessionDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Room database
        chatSessionDatabase = ChatSessionDatabase.getInstance(this);

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

        // Load chat history on startup
        loadChatHistory();

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
            chatMessages.clear();  // Clear the chat messages
            chatAdapter.notifyDataSetChanged();
            saveChatMessages();  // Save the new empty state to SharedPreferences
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

        // Restore chat messages if available
        restoreChatMessages();
    }

    private void saveCurrentSession() {
        String sessionLabel = "Session " + (chatHistoryLabels.size() + 1);
        List<ChatMessage> currentMessages = new ArrayList<>(chatMessages);
        ChatSession chatSession = new ChatSession(sessionLabel, currentMessages);

        new Thread(() -> {
            chatSessionDatabase.chatSessionDao().insertSession(chatSession);
            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this, "Session saved", Toast.LENGTH_SHORT).show();
                loadChatHistory(); // Reload chat history labels after saving
            });
        }).start();
    }

    private void loadChatHistory() {
        new Thread(() -> {
            List<ChatSession> allSessions = chatSessionDatabase.chatSessionDao().getAllSessions();
            chatHistoryLabels.clear();

            for (ChatSession session : allSessions) {
                Log.d("ChatActivity", "Session loaded: " + session.getSessionLabel());
                chatHistoryLabels.add(session.getSessionLabel());
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatHistoryLabels);
                chatHistoryList.setAdapter(adapter);
                Log.d("ChatActivity", "Chat history labels size: " + chatHistoryLabels.size());
            });
        }).start();
    }

    private void loadChatSession(String sessionLabel) {
        new Thread(() -> {
            ChatSession session = chatSessionDatabase.chatSessionDao().getSessionByLabel(sessionLabel);

            if (session != null) {
                chatMessages.clear();
                chatMessages.addAll(session.getMessages());

                runOnUiThread(() -> {
                    chatAdapter.notifyDataSetChanged();
                    if (!chatMessages.isEmpty()) {
                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                    }
                });
            }
        }).start();
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
                        Log.d("ChatActivity", "Raw bot response: " + botResponse);

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
                                    Log.e("ChatActivity", "No 'message' or 'content' in the choice object");
                                }
                            } else {
                                Log.e("ChatActivity", "Empty 'choices' array");
                            }
                        } else {
                            Log.e("ChatActivity", "No 'choices' array in the response");
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ChatActivity", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ChatActivity", "Error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveChatMessages();  // Save chat messages when the activity is paused (e.g., user navigates away)
    }

    private void saveChatMessages() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder messagesString = new StringBuilder();
        for (ChatMessage message : chatMessages) {
            messagesString.append(message.getSender()).append(":").append(message.getContent()).append(";");
        }

        editor.putString(CHAT_MESSAGES_KEY, messagesString.toString());
        editor.apply();
    }

    private void restoreChatMessages() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String savedMessages = sharedPreferences.getString(CHAT_MESSAGES_KEY, "");

        if (!savedMessages.isEmpty()) {
            chatMessages.clear();
            String[] messagesArray = savedMessages.split(";");

            for (String messagePair : messagesArray) {
                if (!messagePair.isEmpty()) {
                    String[] parts = messagePair.split(":");
                    if (parts.length == 2) {
                        chatMessages.add(new ChatMessage(parts[0], parts[1]));
                    }
                }
            }

            chatAdapter.notifyDataSetChanged();
            if (!chatMessages.isEmpty()) {
                recyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        }
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