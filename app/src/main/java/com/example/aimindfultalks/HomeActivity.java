package com.example.aimindfultalks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button chatButton, logEmotionButton, profileButton, resourceLibraryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons
        chatButton = findViewById(R.id.chatButton);
        logEmotionButton = findViewById(R.id.logEmotionButton);
        profileButton = findViewById(R.id.profileButton);
        resourceLibraryButton = findViewById(R.id.resourceLibraryButton);

        // Set button click listeners
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the ChatActivity for chatbot interaction
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        logEmotionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the LogEmotionActivity
                Intent intent = new Intent(HomeActivity.this, LogEmotionActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the ProfileActivity
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        resourceLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the ResourceLibraryActivity
                Intent intent = new Intent(HomeActivity.this, ResourceLibraryActivity.class);
                startActivity(intent);
            }
        });
    }
}