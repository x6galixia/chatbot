package com.example.aimindfultalks;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstNameEditText, middleNameEditText, lastNameEditText, usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private TextView loginLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.first_name);
        middleNameEditText = findViewById(R.id.middle_name);
        lastNameEditText = findViewById(R.id.last_name);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstNameEditText.getText().toString().trim();
                String middleName = middleNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(firstName)) {
                    firstNameEditText.setError("First name is required.");
                    return;
                }

                if (TextUtils.isEmpty(lastName)) {
                    lastNameEditText.setError("Last name is required.");
                    return;
                }

                if (TextUtils.isEmpty(username)) {
                    usernameEditText.setError("Username is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required.");
                    return;
                }

                if (password.length() < 6) {
                    passwordEditText.setError("Password must be at least 6 characters.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    confirmPasswordEditText.setError("Passwords do not match.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Create user using Firebase Auth (use username@yourapp.com as a workaround for email requirement)
                mAuth.createUserWithEmailAndPassword(username + "@yourapp.com", password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // User successfully created
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Save the user's details (first name, middle name, last name, and username) in Firestore
                                    saveUserDetails(user.getUid(), firstName, middleName, lastName, username);

                                    // Redirect to LoginActivity
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                }
                            } else {
                                Toast.makeText(SignUpActivity.this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        });
            }
        });

        // Navigate to LoginActivity if the user already has an account
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void saveUserDetails(String userId, String firstName, String middleName, String lastName, String username) {
        // Save the user details in Firestore under a "users" collection
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("middleName", middleName);
        user.put("lastName", lastName);
        user.put("username", username);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user details
                    Toast.makeText(SignUpActivity.this, "User details saved.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error saving user details
                    Toast.makeText(SignUpActivity.this, "Error saving user details.", Toast.LENGTH_SHORT).show();
                });
    }
}