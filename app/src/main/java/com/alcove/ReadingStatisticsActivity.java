package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.ReadingStatisticsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingStatisticsActivity extends AppCompatActivity {

    private TextView booksReadThisYearText;
    private ProgressBar yearlyProgressBar;
    private TextView yearlyGoalText;
    private TextView currentMonthProgressText;
    private TextView totalBooksReadText;
    private TextView currentlyReadingText;
    private TextView averageRatingText;
    private TextView favoriteGenreText;
    private TextView monthlyGoalText;
    private Button editGoalsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_statistics);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        initializeViews();

        // Load statistics
        loadStatistics();
    }

    private void initializeViews() {
        booksReadThisYearText = findViewById(R.id.booksReadThisYearText);
        yearlyProgressBar = findViewById(R.id.yearlyProgressBar);
        yearlyGoalText = findViewById(R.id.yearlyGoalText);
        currentMonthProgressText = findViewById(R.id.currentMonthProgressText);
        totalBooksReadText = findViewById(R.id.totalBooksReadText);
        currentlyReadingText = findViewById(R.id.currentlyReadingText);
        averageRatingText = findViewById(R.id.averageRatingText);
        favoriteGenreText = findViewById(R.id.favoriteGenreText);
        monthlyGoalText = findViewById(R.id.monthlyGoalText);
        editGoalsBtn = findViewById(R.id.editGoalsBtn);

        editGoalsBtn.setOnClickListener(v -> openPreferencesDialog());
    }

    private void loadStatistics() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserStatistics().enqueue(new Callback<ReadingStatisticsResponse>() {
            @Override
            public void onResponse(Call<ReadingStatisticsResponse> call, Response<ReadingStatisticsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReadingStatisticsResponse stats = response.body();
                    updateUIWithStatistics(stats);
                } else {
                    Toast.makeText(ReadingStatisticsActivity.this, "Failed to load statistics", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReadingStatisticsResponse> call, Throwable t) {
                Toast.makeText(ReadingStatisticsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithStatistics(ReadingStatisticsResponse stats) {
        // This year progress
        int booksThisYear = stats.getBooksReadThisYear();
        int monthlyGoal = stats.getMonthlyReadingGoal();
        booksReadThisYearText.setText("Books read: " + booksThisYear);
        yearlyProgressBar.setMax(monthlyGoal * 12); // Assuming yearly goal is 12x monthly
        yearlyProgressBar.setProgress(booksThisYear);
        yearlyGoalText.setText("Goal: " + (monthlyGoal * 12) + " books");

        // Current month progress
        currentMonthProgressText.setText("Books read: " + stats.getCurrentMonthProgress());

        // Overall stats
        totalBooksReadText.setText(String.valueOf(stats.getTotalBooksRead()));
        currentlyReadingText.setText(String.valueOf(stats.getCurrentlyReading()));

        // Average rating
        Float avgRating = stats.getAverageRatingGiven();
        if (avgRating != null) {
            averageRatingText.setText(String.format("%.1f", avgRating));
        } else {
            averageRatingText.setText("0.0");
        }

        // Favorite genre
        String favoriteGenre = stats.getFavoriteGenre();
        if (favoriteGenre != null && !favoriteGenre.isEmpty()) {
            favoriteGenreText.setText(favoriteGenre);
        } else {
            favoriteGenreText.setText("None");
        }

        // Monthly goal
        monthlyGoalText.setText("Monthly goal: " + monthlyGoal + " books");
    }

    private void openPreferencesDialog() {
        // Open the preferences dialog from UserProfileActivity
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("open_preferences", true);
        startActivity(intent);
    }
}
