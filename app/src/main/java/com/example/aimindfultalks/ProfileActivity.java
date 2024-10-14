package com.example.aimindfultalks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView usernameTextView;
    private Button logoutButton;
    private Button deleteAccountButton;
    private EditText passwordEditText;

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
        usernameTextView = findViewById(R.id.usernameTextView);
        logoutButton = findViewById(R.id.logoutButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        passwordEditText = new EditText(this); // Create EditText for password input

        // Retrieve and display user name and username
        loadUserName();

        // Set up the logout button action
        logoutButton.setOnClickListener(v -> logoutUser());

        // Set up the delete account button action
        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            listenerRegistration = db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Toast.makeText(ProfileActivity.this, "Error retrieving user details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String firstName = snapshot.getString("firstName");
                    String middleName = snapshot.getString("middleName");
                    String lastName = snapshot.getString("lastName");
                    String username = snapshot.getString("username");

                    StringBuilder fullName = new StringBuilder();
                    if (firstName != null) fullName.append(firstName);
                    if (middleName != null && !middleName.isEmpty()) {
                        fullName.append(" ").append(middleName);
                    }
                    if (lastName != null) {
                        fullName.append(" ").append(lastName);
                    }

                    userNameTextView.setText(fullName.toString().trim());
                    usernameTextView.setText(username != null ? username : "No username found");
                } else {
                    userNameTextView.setText("User does not exist.");
                    usernameTextView.setText("No username found");
                }
            });
        } else {
            userNameTextView.setText("User not logged in.");
            usernameTextView.setText("No username found");
        }
    }

    private void confirmDeleteAccount() {
        // Set up the password input field
        passwordEditText.setHint("Enter your password");

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete Account")
                .setMessage("Please enter your password to confirm account deletion.")
                .setView(passwordEditText) // Set the password input in the dialog
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    passwordEditText.setText(""); // Clear the password field
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String password = passwordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required.");
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            // Optionally delete chat sessions from local storage
                            new Thread(() -> {
                                ChatSessionDatabase chatSessionDatabase = ChatSessionDatabase.getInstance(this);
                                chatSessionDatabase.chatSessionDao().deleteAllSessions();  // Clear the chat sessions from Room
                                runOnUiThread(() -> {
                                    Toast.makeText(ProfileActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                    logoutUser(); // Log out the user after deletion
                                });
                            }).start();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to delete account. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Re-authentication failed. Check your password.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}