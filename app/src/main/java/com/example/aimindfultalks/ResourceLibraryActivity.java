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
import java.util.Collections;
import java.util.List;

public class ResourceLibraryActivity extends AppCompatActivity {

    private RecyclerView resourcesRecyclerView;
    private ResourceAdapter resourceAdapter;
    private List<String> resourceList = new ArrayList<>();
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_library);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        resourcesRecyclerView = findViewById(R.id.resourcesRecyclerView);
        resourcesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourceAdapter = new ResourceAdapter(resourceList);
        resourcesRecyclerView.setAdapter(resourceAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    getResourcesFromBot(query);
                }
            }
        });
    }

    private void getResourcesFromBot(String query) {
        // Create the request body for Hugging Face API
        String json = "{\n" +
                "    \"model\": \"meta-llama/Meta-Llama-3-8B-Instruct\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"" + query + "\"}],\n" +
                "    \"max_tokens\": 500,\n" +
                "    \"stream\": false\n" +
                "}";

        RequestBody requestBody = RequestBody.create(json, okhttp3.MediaType.parse("application/json"));
        String apiKey = "Bearer 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"; // Replace with your actual key

        HuggingFaceApi huggingFaceApi = RetrofitClient.getRetrofitInstance().create(HuggingFaceApi.class);
        Call<ResponseBody> call = huggingFaceApi.getChatbotResponse(requestBody, apiKey);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String botResponse = response.body().string();
                        // Log the raw JSON response for debugging
                        Log.d("ResourceLibraryActivity", "Raw bot response: " + botResponse);

                        // Try parsing the JSON structure and extract the content
                        JSONObject jsonResponse = new JSONObject(botResponse);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        String botMessage = choices.getJSONObject(0).getJSONObject("message").getString("content");

                        // Assume the bot response contains resources as a comma-separated list
                        String[] resources = botMessage.split(",");
                        resourceList.clear();
                        Collections.addAll(resourceList, resources);
                        resourceAdapter.notifyDataSetChanged();

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ResourceLibraryActivity", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ResourceLibraryActivity", "Error: " + t.getMessage());
            }
        });
    }
}