package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

// Firebase imports removed for demo mode (auth disabled)

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button signUpButton;
    private TextView loginLink;

    // FirebaseAuth instance
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
        loginLink = findViewById(R.id.loginLink);

        // Sign up button click listener
        signUpButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for at least one uppercase, one lowercase, one digit
            if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
                Toast.makeText(RegisterActivity.this, "Password must contain at least one uppercase letter, one lowercase letter, and one number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable UI while registering
            setUiEnabled(false);

            // Use Firebase Authentication to create the user
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(this, authResult -> {
                        setUiEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    })
                    .addOnFailureListener(this, e -> {
                        setUiEnabled(true);
                        String msg = mapRegisterExceptionToMessage(e);
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    });
        });

        // Login link click listener
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        // Clear back stack to avoid returning to register/login via back
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setUiEnabled(boolean enabled) {
        signUpButton.setEnabled(enabled);
        loginLink.setEnabled(enabled);
        nameInput.setEnabled(enabled);
        emailInput.setEnabled(enabled);
        passwordInput.setEnabled(enabled);
        confirmPasswordInput.setEnabled(enabled);
    }

    private String mapRegisterExceptionToMessage(Exception e) {
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            String errorCode = authException.getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "The email address is badly formatted.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "The email address is already in use by another account.";
                case "ERROR_WEAK_PASSWORD":
                    return "The password is too weak.";
                default:
                    return "Registration failed. Please try again.";
            }
        }
        return "Registration failed. Please try again.";
    }
}
