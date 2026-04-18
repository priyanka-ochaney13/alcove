package com.alcove;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private Switch notificationsSwitch;
    private Button changePasswordBtn;
    private Button aboutBtn;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        aboutBtn = findViewById(R.id.aboutBtn);
        backButton = findViewById(R.id.backButton);

        // Notifications toggle
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(SettingsActivity.this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        // Change password button
        changePasswordBtn.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Change password feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // About button
        aboutBtn.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Goodreads Clone v1.0", Toast.LENGTH_SHORT).show();
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
    }
}
