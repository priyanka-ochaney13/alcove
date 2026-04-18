package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.CreateReviewRequest;
import com.alcove.models.ReviewResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ReviewActivity - Allows users to write and submit reviews for books
 */
public class ReviewActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "bookId";

    private TextView bookTitleText;
    private RatingBar reviewRatingBar;
    private EditText reviewTitleEdit;
    private EditText reviewContentEdit;
    private Button submitBtn;
    private Button cancelBtn;

    private int bookId;
    private String bookTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_write_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Extract intent data
        extractIntentData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        bookTitleText = findViewById(R.id.bookTitleText);
        reviewRatingBar = findViewById(R.id.reviewRatingBar);
        reviewTitleEdit = findViewById(R.id.reviewTitleEdit);
        reviewContentEdit = findViewById(R.id.reviewContentEdit);
        submitBtn = findViewById(R.id.submitBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        bookId = intent.getIntExtra(EXTRA_BOOK_ID, -1);
        bookTitle = intent.getStringExtra("bookTitle");

        if (bookId <= 0) {
            Toast.makeText(this, "Invalid book", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (bookTitle != null) {
            bookTitleText.setText("Review: " + bookTitle);
        }
    }

    private void setupClickListeners() {
        cancelBtn.setOnClickListener(v -> finish());

        submitBtn.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        // Validate input
        String content = reviewContentEdit.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = reviewRatingBar.getRating();
        if (rating < 1) {
            Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = reviewTitleEdit.getText().toString().trim();
        final String finalTitle = title.isEmpty() ? null : title;

        // Create review request
        CreateReviewRequest request = new CreateReviewRequest();
        request.setContent(content);
        request.setTitle(finalTitle);
        request.setRating((int) rating);

        // Submit review
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createReview(bookId, request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReviewActivity.this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Return to book details and refresh
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("reviewAdded", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(ReviewActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Toast.makeText(ReviewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
