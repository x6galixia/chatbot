package com.example.aimindfultalks;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;  // Change to List<ChatMessage>
    private EditText messageEditText;
    private Button sendButton;
    private Button newSessionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize chat messages list and adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        // Initialize EditText and Buttons
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        newSessionButton = findViewById(R.id.newSessionButton);

        // Set onClickListener for Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString();
                if (!message.isEmpty()) {
                    chatMessages.add(new ChatMessage("user", message));  // Store as ChatMessage
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                    messageEditText.setText("");

                    // Send the message to the chatbot
                    sendMessageToChatbot(message);
                }
            }
        });

        // Set onClickListener for New Session Button
        newSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear chat messages
                chatMessages.clear();
                chatAdapter.notifyDataSetChanged();
            }
        });
    }

    private void sendMessageToChatbot(String message) {
        // Create the request body for Hugging Face API
        String json = "{\n" +
                "    \"model\": \"meta-llama/Meta-Llama-3-8B-Instruct\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}],\n" +
                "    \"max_tokens\": 500,\n" +
                "    \"stream\": false\n" +
                "}";

        RequestBody requestBody = RequestBody.create(json, okhttp3.MediaType.parse("application/json"));
        String apiKey = "Bearer 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";

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
}