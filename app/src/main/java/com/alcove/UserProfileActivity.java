package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alcove.dummy.DummyDataGenerator;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private TextView booksReadCount;
    private TextView currentlyReadingCount;
    private TextView wantToReadCount;
    private Button editProfileBtn;
    private Button myReviewsBtn;
    private Button settingsBtn;
    private Button logoutBtn;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        booksReadCount = findViewById(R.id.booksReadCount);
        currentlyReadingCount = findViewById(R.id.currentlyReadingCount);
        wantToReadCount = findViewById(R.id.wantToReadCount);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        myReviewsBtn = findViewById(R.id.myReviewsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        backButton = findViewById(R.id.backButton);

        // Set dummy data
        DummyDataGenerator.UserProfile profile = DummyDataGenerator.getUserProfile();
        userName.setText(profile.name);
        userEmail.setText(profile.email);
        booksReadCount.setText(String.valueOf(profile.booksRead));
        currentlyReadingCount.setText(String.valueOf(profile.currentlyReading));
        wantToReadCount.setText(String.valueOf(profile.wantToRead));

        // Button listeners
        backButton.setOnClickListener(v -> finish());

        editProfileBtn.setOnClickListener(v -> {
            // TODO: Navigate to edit profile
        });

        myReviewsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, MyReviewsActivity.class);
            startActivity(intent);
        });

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            // TODO: Logout and return to login screen
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
