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

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView signUpLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge: disable fitting system windows so content can draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpLink = findViewById(R.id.signUpLink);
        mAuth = FirebaseAuth.getInstance();

        // Login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable UI while signing in
            setUiEnabled(false);

            // Use Firebase Authentication to sign in the user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(this, authResult -> {
                        final boolean[] syncDone = {false};
                        authResult.getUser().getIdToken(true).addOnSuccessListener(tokenResult -> {
                            if (syncDone[0]) return;
                            syncDone[0] = true;

                            com.alcove.models.FirebaseSyncRequest syncRequest = new com.alcove.models.FirebaseSyncRequest();
                            syncRequest.setToken(tokenResult.getToken());
                            com.alcove.api.ApiClient.getService().syncUser(syncRequest).enqueue(new retrofit2.Callback<com.alcove.models.FirebaseSyncResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.alcove.models.FirebaseSyncResponse> call, retrofit2.Response<com.alcove.models.FirebaseSyncResponse> response) {
                                    setUiEnabled(true);
                                    if (response.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Backend sync failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<com.alcove.models.FirebaseSyncResponse> call, Throwable t) {
                                    setUiEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Backend sync failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .addOnFailureListener(this, e -> {
                        setUiEnabled(true);
                        String msg = mapAuthExceptionToMessage(e);
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    });
        });

        // Sign up link click listener
        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // User is signed in, go to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setUiEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        signUpLink.setEnabled(enabled);
        emailInput.setEnabled(enabled);
        passwordInput.setEnabled(enabled);
    }

    private String mapAuthExceptionToMessage(Exception e) {
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            String errorCode = authException.getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "The email address is badly formatted.";
                case "ERROR_WRONG_PASSWORD":
                    return "The password is invalid or the user does not have a password.";
                case "ERROR_USER_NOT_FOUND":
                    return "There is no user record corresponding to this email.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "The email address is already in use by another account.";
                // Add more cases as needed
                default:
                    return "Authentication failed. Please try again.";
            }
        }
        return "Authentication failed. Please try again.";
    }
}
