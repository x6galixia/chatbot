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

    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
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

        fullNameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = fullNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)) {
                    fullNameEditText.setError("Full name is required.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    confirmPasswordEditText.setError("Passwords do not match.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Create user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // User successfully created
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Save the user's full name in Firestore
                                    saveUserFullName(user.getUid(), fullName);

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

    private void saveUserFullName(String userId, String fullName) {
        // Save the full name in Firestore under a "users" collection
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);

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