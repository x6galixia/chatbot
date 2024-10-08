package com.example.aimindfultalks;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class LogEmotionActivity extends AppCompatActivity {

    private EditText botResponseEditText;
    private EditText noteEditText;
    private Button submitButton;

    // Variables to hold the selected emotion
    private String selectedEmotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_emotion);

        // Initialize Views
        botResponseEditText = findViewById(R.id.botResponseEditText);
        noteEditText = findViewById(R.id.noteEditText);
        submitButton = findViewById(R.id.submitButton);

        // Set up emotion buttons
        Button buttonHappy = findViewById(R.id.button_happy);
        Button buttonSad = findViewById(R.id.button_sad);
        Button buttonAnxious = findViewById(R.id.button_anxious);
        Button buttonFear = findViewById(R.id.button_fear);

        buttonHappy.setOnClickListener(v -> selectEmotion("happy"));
        buttonSad.setOnClickListener(v -> selectEmotion("sad"));
        buttonAnxious.setOnClickListener(v -> selectEmotion("anxious"));
        buttonFear.setOnClickListener(v -> selectEmotion("fear"));

        // Set up button click listener for the submit button
        submitButton.setOnClickListener(v -> {
            String note = noteEditText.getText().toString();
            if (selectedEmotion != null) {
                sendEmotionToChatbot(selectedEmotion, note);
            } else {
                Toast.makeText(LogEmotionActivity.this, "Please select an emotion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectEmotion(String emotion) {
        selectedEmotion = emotion;
        Toast.makeText(this, "Selected Emotion: " + emotion, Toast.LENGTH_SHORT).show();
    }

    private void sendEmotionToChatbot(String emotion, String note) {
        // Create the request body for the Hugging Face API
        String json = "{\n" +
                "    \"model\": \"meta-llama/Meta-Llama-3-8B-Instruct\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"I feel " + emotion + ". " + note + "\"}],\n" +
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
                        Log.d("LogEmotionActivity", "Raw bot response: " + botResponse);

                        // Parse the JSON response to extract the message
                        String reply = parseBotReply(botResponse);
                        botResponseEditText.setText(reply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("LogEmotionActivity", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("LogEmotionActivity", "Error: " + t.getMessage());
            }
        });
    }

    private String parseBotReply(String botResponse) {
        String reply = "";
        try {
            JSONObject jsonResponse = new JSONObject(botResponse);
            if (jsonResponse.has("choices")) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                        reply = choice.getJSONObject("message").getString("content");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reply;
    }
}