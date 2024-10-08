package com.example.aimindfultalks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        userNameTextView = findViewById(R.id.userNameTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Retrieve and display user name
        loadUserName();

        // Set up the logout button action
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Listen for the user's document in Firestore
            listenerRegistration = db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Toast.makeText(ProfileActivity.this, "Error retrieving user details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String fullName = snapshot.getString("fullName");
                    if (fullName != null) {
                        userNameTextView.setText(fullName);
                    } else {
                        userNameTextView.setText("No name found");
                    }
                } else {
                    userNameTextView.setText("User does not exist.");
                }
            });
        } else {
            userNameTextView.setText("User not logged in.");
        }
    }

    private void logoutUser() {
        // Logic to log out the user
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the ProfileActivity
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove the listener to prevent memory leaks
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}