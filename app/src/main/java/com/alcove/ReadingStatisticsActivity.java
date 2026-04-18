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
    private TextView totalBooksReadText;
    private TextView currentlyReadingText;
    private TextView averageRatingText;
    private TextView monthlyGoalText;
    private TextView wantToReadText;
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
        totalBooksReadText = findViewById(R.id.totalBooksReadText);
        currentlyReadingText = findViewById(R.id.currentlyReadingText);
        averageRatingText = findViewById(R.id.averageRatingText);
        monthlyGoalText = findViewById(R.id.monthlyGoalText);
        wantToReadText = findViewById(R.id.wantToReadText);
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
                    String errorMsg = "Failed to load statistics";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(ReadingStatisticsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ReadingStatisticsResponse> call, Throwable t) {
                Toast.makeText(ReadingStatisticsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUIWithStatistics(ReadingStatisticsResponse stats) {
        // This year progress
        int booksThisYear = stats.getBooksReadThisYear();
        int yearlyGoal = stats.getYearlyReadingGoal();
        booksReadThisYearText.setText("Books read: " + booksThisYear);
        yearlyProgressBar.setIndeterminate(false);
        yearlyProgressBar.setMax(yearlyGoal > 0 ? yearlyGoal : 1);
        yearlyProgressBar.setProgress(Math.min(booksThisYear, yearlyGoal > 0 ? yearlyGoal : 1));
        yearlyGoalText.setText("Goal: " + yearlyGoal + " books");

        // Overall stats
        totalBooksReadText.setText(String.valueOf(stats.getTotalBooksRead()));
        currentlyReadingText.setText(String.valueOf(stats.getCurrentlyReading()));
        wantToReadText.setText(String.valueOf(stats.getWantToRead()));

        // Average rating
        Float avgRating = stats.getAverageRatingGiven();
        averageRatingText.setText(avgRating != null ? String.format("%.1f", avgRating) : "—");

        // Monthly goal
        monthlyGoalText.setText("Monthly goal: " + stats.getMonthlyReadingGoal() + " books");
    }

    private void openPreferencesDialog() {
        // Open the preferences dialog from UserProfileActivity
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("open_preferences", true);
        startActivity(intent);
    }
}
