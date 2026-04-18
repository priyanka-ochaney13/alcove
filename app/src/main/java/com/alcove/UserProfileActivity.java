package com.alcove;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import com.alcove.services.LocationManager;
import com.alcove.services.ReadingReminderWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private TextView userLocation;
    private Button editProfileBtn;
    private Button myReviewsBtn;
    private Button settingsBtn;
    private Button logoutBtn;
    private ImageView backButton;
    private Button changeProfilePicBtn;
    private Button enableLocationBtn;
    private TextView userBio;
    private Button preferencesBtn;
    private Button readingStatsBtn;

    private LocationManager locationManager;
    private Location userLocation_data;

    // Activity result launchers
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    // Current user profile data
    private com.alcove.models.UserResponse currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize location manager
        locationManager = new LocationManager(this);

        // Setup activity result launchers
        setupActivityResultLaunchers();

        // Schedule Reading Reminder Worker
        scheduleReadingReminderNotification();

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userLocation = findViewById(R.id.userLocation);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        myReviewsBtn = findViewById(R.id.myReviewsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        backButton = findViewById(R.id.backButton);
        changeProfilePicBtn = findViewById(R.id.changeProfilePicBtn);
        changeProfilePicBtn.setVisibility(android.view.View.GONE); // hide it until real upload is implemented
        enableLocationBtn = findViewById(R.id.enableLocationBtn);
        userBio = findViewById(R.id.userBio);
        preferencesBtn = findViewById(R.id.preferencesBtn);
        readingStatsBtn = findViewById(R.id.readingStatsBtn);


        // Populate name/email/photo with placeholder values (auth disabled in demo mode)
        updateProfileFromFirebase();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Button submitBookBtn = findViewById(R.id.submitBookBtn);
        if (firebaseUser != null && "veschwab@gmail.com".equals(firebaseUser.getEmail())) {
            submitBookBtn.setVisibility(android.view.View.VISIBLE);
            submitBookBtn.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, SubmitBookActivity.class)));
        } else {
            submitBookBtn.setVisibility(android.view.View.GONE);
        }

        // Button listeners
        backButton.setOnClickListener(v -> finish());

        editProfileBtn.setOnClickListener(v -> showEditProfileDialog());

        changeProfilePicBtn.setOnClickListener(v -> pickImage());

        enableLocationBtn.setOnClickListener(v -> requestLocationAndFetch());

        myReviewsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, MyReviewsActivity.class);
            startActivity(intent);
        });

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        preferencesBtn.setOnClickListener(v -> showPreferencesDialog());

        readingStatsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, ReadingStatisticsActivity.class);
            startActivity(intent);
        });

        // Check if we need to open preferences dialog
        if (getIntent().getBooleanExtra("open_preferences", false)) {
            preferencesBtn.post(this::showPreferencesDialog);
        }
    }

    /**
     * Setup activity result launchers for picking images and requesting permissions
     */
    private void setupActivityResultLaunchers() {
        // Image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        updateProfilePictureInFirebase(uri);
                    }
                });

        // Permission request launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean fineLocationGranted = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarseLocationGranted = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                    if (fineLocationGranted || coarseLocationGranted) {
                        fetchUserLocation();
                    } else {
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Notification permission request launcher for Android 13+
        requestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void scheduleReadingReminderNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        PeriodicWorkRequest readingReminder = new PeriodicWorkRequest.Builder(
                ReadingReminderWorker.class,
                24, // Once a day 
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "reading_reminder_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                readingReminder
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileFromFirebase();
    }

    private void updateProfileFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            userEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            // Load profile image if available, else use default
            if (user.getPhotoUrl() != null) {
                // Use Glide to load profile image
                com.bumptech.glide.Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }

            // Load extended profile data from backend API
            loadUserProfileFromAPI();
        } else {
            // Not logged in, show guest
            userName.setText("Guest User");
            userEmail.setText("");
            profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    private void loadUserProfileFromAPI() {
        com.alcove.api.ApiService apiService = com.alcove.api.ApiClient.getClient().create(com.alcove.api.ApiService.class);
        apiService.getUserProfile().enqueue(new retrofit2.Callback<com.alcove.models.UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.alcove.models.UserResponse> call, retrofit2.Response<com.alcove.models.UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.alcove.models.UserResponse userProfile = response.body();

                    // Update UI with backend data
                    if (userProfile.getFullName() != null && !userProfile.getFullName().isEmpty()) {
                        userName.setText(userProfile.getFullName());
                    } else if (userProfile.getUsername() != null) {
                        userName.setText(userProfile.getUsername());
                    }

                    if (userProfile.getBio() != null && !userProfile.getBio().isEmpty()) {
                        userBio.setText(userProfile.getBio());
                        userBio.setVisibility(android.view.View.VISIBLE);
                    } else {
                        userBio.setVisibility(android.view.View.GONE);
                    }

                    // Store profile data for edit dialog
                    currentUserProfile = userProfile;
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.alcove.models.UserResponse> call, Throwable t) {
                // Silently fail - Firebase data is already loaded
                android.util.Log.e("UserProfileActivity", "Failed to load profile from API", t);
            }
        });
    }


    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        // Inflate the custom dialog layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        // Get references to the input fields
        EditText fullNameEdit = dialogView.findViewById(R.id.fullNameEdit);
        EditText usernameEdit = dialogView.findViewById(R.id.usernameEdit);
        EditText emailEdit = dialogView.findViewById(R.id.emailEdit);
        EditText bioEdit = dialogView.findViewById(R.id.bioEdit);

        // Pre-populate fields with current values
        if (currentUserProfile != null) {
            fullNameEdit.setText(currentUserProfile.getFullName() != null ? currentUserProfile.getFullName() : "");
            usernameEdit.setText(currentUserProfile.getUsername() != null ? currentUserProfile.getUsername() : "");
            emailEdit.setText(currentUserProfile.getEmail() != null ? currentUserProfile.getEmail() : "");
            bioEdit.setText(currentUserProfile.getBio() != null ? currentUserProfile.getBio() : "");
        } else {
            // Fallback to Firebase data if API data not loaded yet
            fullNameEdit.setText(userName.getText().toString());
            emailEdit.setText(userEmail.getText().toString());
            usernameEdit.setText(""); // No username from Firebase
            bioEdit.setText("");
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            // Collect the updated values
            String newFullName = fullNameEdit.getText().toString().trim();
            String newUsername = usernameEdit.getText().toString().trim();
            String newEmail = emailEdit.getText().toString().trim();
            String newBio = bioEdit.getText().toString().trim();

            // Validate inputs
            if (newFullName.isEmpty()) {
                Toast.makeText(this, "Full name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newEmail.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update profile via API
            updateUserProfile(newFullName, newUsername, newEmail, newBio);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateUserProfile(String fullName, String username, String email, String bio) {
        // Create update request
        com.alcove.models.UserUpdateRequest request = new com.alcove.models.UserUpdateRequest();
        request.setFullName(fullName);
        request.setUsername(username);
        request.setEmail(email);
        request.setBio(bio);

        // Call API to update profile
        com.alcove.api.ApiService apiService = com.alcove.api.ApiClient.getClient().create(com.alcove.api.ApiService.class);
        apiService.updateUserProfile(request).enqueue(new retrofit2.Callback<com.alcove.models.UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.alcove.models.UserResponse> call, retrofit2.Response<com.alcove.models.UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update UI with new profile data
                    com.alcove.models.UserResponse updatedUser = response.body();
                    userName.setText(updatedUser.getFullName() != null ? updatedUser.getFullName() : updatedUser.getUsername());
                    userEmail.setText(updatedUser.getEmail());

                    if (updatedUser.getBio() != null && !updatedUser.getBio().isEmpty()) {
                        userBio.setText(updatedUser.getBio());
                        userBio.setVisibility(android.view.View.VISIBLE);
                    } else {
                        userBio.setVisibility(android.view.View.GONE);
                    }

                    Toast.makeText(UserProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.alcove.models.UserResponse> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Open image picker to select profile picture
     */
    private void pickImage() {
        pickImageLauncher.launch("image/*");
    }

    /**
     * Update profile picture in Firebase
     */
    private void updateProfilePictureInFirebase(Uri imageUri) {
        // For demo purposes, we'll simulate uploading to a service and getting a URL
        // In a real app, you'd upload to Firebase Storage or another service
        String avatarUrl = "https://example.com/avatar/" + System.currentTimeMillis() + ".jpg";

        // Update profile with new avatar URL
        com.alcove.models.UserUpdateRequest request = new com.alcove.models.UserUpdateRequest();
        request.setAvatarUrl(avatarUrl);

        com.alcove.api.ApiService apiService = com.alcove.api.ApiClient.getClient().create(com.alcove.api.ApiService.class);
        apiService.updateUserProfile(request).enqueue(new retrofit2.Callback<com.alcove.models.UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.alcove.models.UserResponse> call, retrofit2.Response<com.alcove.models.UserResponse> response) {
                if (response.isSuccessful()) {
                    // Update local UI
                    profileImage.setImageURI(imageUri);
                    Toast.makeText(UserProfileActivity.this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.alcove.models.UserResponse> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Request location permissions and fetch GPS location
     */
    private void requestLocationAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation();
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Fetch current user location from GPS
     */
    private void fetchUserLocation() {
        Toast.makeText(this, "Fetching your location...", Toast.LENGTH_SHORT).show();

        locationManager.getCurrentLocation(new LocationManager.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                userLocation_data = location;
                LocationManager.getLocationName(UserProfileActivity.this, location, new LocationManager.LocationNameCallback() {
                    @Override
                    public void onLocationNameReceived(String name) {
                        String locationText = "📍 " + name;
                        runOnUiThread(() -> {
                            userLocation.setText(locationText);
                            userLocation.setVisibility(android.view.View.VISIBLE);
                            Toast.makeText(UserProfileActivity.this, "Location updated!", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPreferencesDialog() {
        // First load current preferences
        com.alcove.api.ApiService apiService = com.alcove.api.ApiClient.getClient().create(com.alcove.api.ApiService.class);
        apiService.getUserPreferences().enqueue(new retrofit2.Callback<com.alcove.models.UserPreferencesResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.alcove.models.UserPreferencesResponse> call, retrofit2.Response<com.alcove.models.UserPreferencesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showPreferencesDialogWithData(response.body());
                } else {
                    // Show dialog with defaults
                    showPreferencesDialogWithData(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.alcove.models.UserPreferencesResponse> call, Throwable t) {
                // Show dialog with defaults
                showPreferencesDialogWithData(null);
            }
        });
    }

    private void showPreferencesDialogWithData(com.alcove.models.UserPreferencesResponse currentPrefs) {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reading Preferences");

        // Create a simple preferences layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        // Monthly reading goal preference
        android.widget.TextView monthlyGoalLabel = new android.widget.TextView(this);
        monthlyGoalLabel.setText("Monthly Reading Goal");
        monthlyGoalLabel.setTextSize(16);
        monthlyGoalLabel.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        monthlyGoalLabel.setPadding(0, 0, 0, 8);
        layout.addView(monthlyGoalLabel);

        android.widget.EditText monthlyGoalInput = new android.widget.EditText(this);
        monthlyGoalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        monthlyGoalInput.setText(currentPrefs != null ? String.valueOf(currentPrefs.getMonthlyReadingGoal()) : "12");
        monthlyGoalInput.setBackgroundResource(R.drawable.rounded_input_background);
        monthlyGoalInput.setPadding(16, 16, 16, 16);
        monthlyGoalInput.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.black));
        layout.addView(monthlyGoalInput);

        // Yearly reading goal preference
        android.widget.TextView yearlyGoalLabel = new android.widget.TextView(this);
        yearlyGoalLabel.setText("\nYearly Reading Goal");
        yearlyGoalLabel.setTextSize(16);
        yearlyGoalLabel.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        yearlyGoalLabel.setPadding(0, 16, 0, 8);
        layout.addView(yearlyGoalLabel);

        android.widget.EditText yearlyGoalInput = new android.widget.EditText(this);
        yearlyGoalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        yearlyGoalInput.setText(currentPrefs != null ? String.valueOf(currentPrefs.getYearlyReadingGoal()) : "0");
        yearlyGoalInput.setBackgroundResource(R.drawable.rounded_input_background);
        yearlyGoalInput.setPadding(16, 16, 16, 16);
        yearlyGoalInput.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.black));
        layout.addView(yearlyGoalInput);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Collect values and save
            try {
                int monthlyGoal = Integer.parseInt(monthlyGoalInput.getText().toString().trim());
                int yearlyGoal = Integer.parseInt(yearlyGoalInput.getText().toString().trim());
                saveUserPreferences(monthlyGoal, yearlyGoal);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid reading goal number", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveUserPreferences(int monthlyGoal, int yearlyGoal) {
        com.alcove.models.UserPreferencesUpdateRequest request = new com.alcove.models.UserPreferencesUpdateRequest();
        request.setMonthlyReadingGoal(monthlyGoal);
        request.setYearlyReadingGoal(yearlyGoal);

        com.alcove.api.ApiService apiService = com.alcove.api.ApiClient.getClient().create(com.alcove.api.ApiService.class);
        apiService.updateUserPreferences(request).enqueue(new retrofit2.Callback<com.alcove.models.UserPreferencesResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.alcove.models.UserPreferencesResponse> call, retrofit2.Response<com.alcove.models.UserPreferencesResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UserProfileActivity.this, "Preferences saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to save preferences", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.alcove.models.UserPreferencesResponse> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
