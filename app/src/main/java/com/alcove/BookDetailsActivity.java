package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.alcove.services.ReadingReminderWorker;
import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.BookDetailResponse;
import com.alcove.models.ShelfStatusResponse;
import com.alcove.models.CustomShelfResponse;
import com.alcove.models.AddToShelfResponse;
import com.alcove.models.AddBookToShelfRequest;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BookDetailsActivity - Demonstrates receiving data via Intent and gesture handling
 * Shows pinch-to-zoom gesture on book cover
 */
public class BookDetailsActivity extends AppCompatActivity {

    // Intent extra keys - best practice to define as constants
    public static final String EXTRA_BOOK_ID = "bookId";
    public static final String EXTRA_BOOK_TITLE = "bookTitle";
    public static final String EXTRA_BOOK_AUTHOR = "bookAuthor";
    public static final String EXTRA_BOOK_RATING = "bookRating";
    public static final String EXTRA_RATING_COUNT = "ratingCount";

    private ImageView bookCover;
    private TextView bookTitle;
    private TextView bookAuthor;
    private RatingBar bookRating;
    private TextView ratingCount;
    private TextView description;
    private Button addToShelfButton;
    private ImageView backButton;
    private RecyclerView reviewsRecycler;
    private ReviewAdapter reviewAdapter;
    private TextView aiSummaryText;
    private androidx.cardview.widget.CardView aiSummaryCard;
    private Button writeReviewBtn;

    private int bookId;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    private final ActivityResultLauncher<Intent> reviewActivityLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                boolean reviewAdded = result.getData().getBooleanExtra("reviewAdded", false);
                if (reviewAdded) {
                    // Refresh the book details to show the new review
                    fetchBookDetailsFromApi(bookId);
                    Toast.makeText(this, "Review added successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_book_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Extract data from Intent
        extractIntentData();

        // Setup reviews
        setupReviews();

        // Setup click listeners
        setupClickListeners();

        // Setup pinch-to-zoom gesture
        setupPinchZoom();
    }

    /**
     * Initialize all view references
     */
    private void initializeViews() {
        bookCover = findViewById(R.id.bookCover);
        bookTitle = findViewById(R.id.bookTitle);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookRating = findViewById(R.id.bookRating);
        ratingCount = findViewById(R.id.ratingCount);
        description = findViewById(R.id.description);
        addToShelfButton = findViewById(R.id.addToShelfButton);
        backButton = findViewById(R.id.backButton);
        reviewsRecycler = findViewById(R.id.reviewsRecycler);
        aiSummaryText = findViewById(R.id.aiSummaryText);
        aiSummaryCard = findViewById(R.id.aiSummaryCard);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
    }

    /**
     * Extract data passed via Intent
     * Demonstrates proper Intent data extraction with default values
     */
    private void extractIntentData() {
        Intent intent = getIntent();

        bookId = intent.getIntExtra(EXTRA_BOOK_ID, -1);
        if (bookId <= 0) {
            Toast.makeText(this, "Invalid book details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String title = intent.getStringExtra(EXTRA_BOOK_TITLE);
        String author = intent.getStringExtra(EXTRA_BOOK_AUTHOR);
        float rating = intent.getFloatExtra(EXTRA_BOOK_RATING, 0.0f);
        int reviewsCount = intent.getIntExtra(EXTRA_RATING_COUNT, 0);
        String imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL");

        // Initial fallbacks
        bookTitle.setText(title != null ? title : "Loading...");
        if (author != null) bookAuthor.setText(author);
        bookRating.setRating(rating);
        ratingCount.setText(getString(R.string.ratings, reviewsCount));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_book_placeholder)
                .error(R.drawable.ic_book_placeholder)
                .into(bookCover);
        } else {
            bookCover.setImageResource(R.drawable.ic_book_placeholder);
        }

        // Fetch pure data from API
        fetchBookDetailsFromApi(bookId);
    }

    private void fetchBookDetailsFromApi(int bookId) {
        ApiClient.getClient().create(ApiService.class).getBookDetails(bookId).enqueue(new Callback<BookDetailResponse>() {
            @Override
            public void onResponse(Call<BookDetailResponse> call, Response<BookDetailResponse> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        BookDetailResponse book = response.body();

                        if (bookTitle != null) bookTitle.setText(book.getTitle());
                        if (bookRating != null) bookRating.setRating((float) book.getAverageRating());

                        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty() && bookCover != null) {
                            Glide.with(BookDetailsActivity.this)
                                .load(book.getImageUrl())
                                .placeholder(R.drawable.ic_book_placeholder)
                                .error(R.drawable.ic_book_placeholder)
                                .into(bookCover);
                        } else if (bookCover != null) {
                            bookCover.setImageResource(R.drawable.ic_book_placeholder);
                        }

                        if (book.getDescription() != null && description != null) {
                            description.setText(book.getDescription());
                        } else if (description != null) {
                            description.setText("No description available");
                        }

                        if (aiSummaryCard != null && aiSummaryText != null) {
                            if (book.getAiReviewSummary() != null && !book.getAiReviewSummary().isEmpty()) {
                                aiSummaryText.setText(book.getAiReviewSummary());
                                aiSummaryCard.setVisibility(android.view.View.VISIBLE);
                            } else {
                                aiSummaryText.setText(getString(R.string.no_ai_summary_yet));
                                aiSummaryCard.setVisibility(android.view.View.VISIBLE);
                            }
                        }

                        if (book.getReviews() != null && !book.getReviews().isEmpty() && reviewsRecycler != null) {
                            List<Review> userReviews = new ArrayList<>();
                            for (com.alcove.models.ReviewResponse r : book.getReviews()) {
                                String reviewer = r.getUser() != null ? r.getUser().getUsername() : "Anonymous";
                                String userId = r.getUser() != null ? String.valueOf(r.getUser().getId()) : "";
                                int rating = r.getRating() != null ? r.getRating().intValue() : 0;
                                userReviews.add(new Review(r.getId(), reviewer, userId, rating, r.getContent()));
                            }
                            reviewAdapter = new ReviewAdapter(BookDetailsActivity.this, userReviews, bookId);
                            reviewsRecycler.setAdapter(reviewAdapter);
                        }

                    } else {
                        Toast.makeText(BookDetailsActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
                        if (aiSummaryCard != null) aiSummaryCard.setVisibility(android.view.View.GONE);
                    }
                } catch (Exception e) {
                    android.util.Log.e("BookDetailsActivity", "Error processing response", e);
                    Toast.makeText(BookDetailsActivity.this, "Error loading details", Toast.LENGTH_SHORT).show();
                    if (aiSummaryCard != null) aiSummaryCard.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<BookDetailResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                android.util.Log.e("BookDetailsActivity", "Network error", t);
                Toast.makeText(BookDetailsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                if (aiSummaryCard != null) aiSummaryCard.setVisibility(android.view.View.GONE);
            }
        });
    }

    /**
     * Setup reviews RecyclerView
     */
    private void setupReviews() {
        // Prepare empty adapter, it will be updated when API call is successful
        List<Review> reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviews, bookId);
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.setAdapter(reviewAdapter);
    }

    /**
     * Setup all click listeners
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Add to shelf button
        addToShelfButton.setOnClickListener(v -> {
            // First, check current shelf status
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.getBookShelfStatus(bookId).enqueue(new Callback<ShelfStatusResponse>() {
                @Override
                public void onResponse(Call<ShelfStatusResponse> call, Response<ShelfStatusResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String currentShelf = response.body().getShelf();
                        if (currentShelf != null && !currentShelf.isEmpty()) {
                            Toast.makeText(BookDetailsActivity.this, "Book is already in '" + currentShelf + "' shelf", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    // If not in any shelf, add to "Want to Read"
                    addToWantToReadShelf();
                }

                @Override
                public void onFailure(Call<ShelfStatusResponse> call, Throwable t) {
                    // On failure, still try to add
                    addToWantToReadShelf();
                }
            });
        });

        // Write review button
        writeReviewBtn.setOnClickListener(v -> {
            // Navigate to review activity
            Intent reviewIntent = new Intent(BookDetailsActivity.this, ReviewActivity.class);
            reviewIntent.putExtra(ReviewActivity.EXTRA_BOOK_ID, bookId);
            reviewIntent.putExtra("bookTitle", bookTitle.getText().toString());
            reviewActivityLauncher.launch(reviewIntent);
        });
    }

    /**
     * Setup pinch-to-zoom gesture for book cover
     */
    private void setupPinchZoom() {
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // Limit the scale factor
                bookCover.setScaleX(scaleFactor);
                bookCover.setScaleY(scaleFactor);
                return true;
            }
        });

        bookCover.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });
    }

    /**
     * Helper method to create Intent for this Activity
     * Best practice: Provide a static method to create Intents
     */
    public static Intent createIntent(android.content.Context context, Book book) {
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, book.getId());
        intent.putExtra(EXTRA_BOOK_TITLE, book.getTitle());
        intent.putExtra(EXTRA_BOOK_AUTHOR, book.getAuthor());
        intent.putExtra(EXTRA_BOOK_RATING, book.getRating());
        intent.putExtra(EXTRA_RATING_COUNT, book.getRatingCount());
        intent.putExtra("EXTRA_IMAGE_URL", book.getImageUrl());
        return intent;
    }

    /**
     * Add the current book to the "Want to Read" shelf
     */
    private void addToWantToReadShelf() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserShelves().enqueue(new Callback<List<CustomShelfResponse>>() {
            @Override
            public void onResponse(Call<List<CustomShelfResponse>> call, Response<List<CustomShelfResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (CustomShelfResponse shelf : response.body()) {
                        if ("Want to Read".equals(shelf.getName())) {
                            // Found the shelf, add the book
                            addBookToShelf(shelf.getId());
                            return;
                        }
                    }
                    Toast.makeText(BookDetailsActivity.this, "Could not find 'Want to Read' shelf", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookDetailsActivity.this, "Failed to load shelves", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CustomShelfResponse>> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookToShelf(int shelfId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        AddBookToShelfRequest request = new AddBookToShelfRequest(bookId);
        apiService.addBookToShelf(shelfId, request).enqueue(new Callback<AddToShelfResponse>() {
            @Override
            public void onResponse(Call<AddToShelfResponse> call, Response<AddToShelfResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailsActivity.this, "Added to your 'Want to Read' shelf!", Toast.LENGTH_SHORT).show();
                    // Trigger background task
                    OneTimeWorkRequest instantWorker = new OneTimeWorkRequest.Builder(ReadingReminderWorker.class).build();
                    WorkManager.getInstance(BookDetailsActivity.this).enqueue(instantWorker);
                    // Pass result back
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("bookAdded", true);
                    resultIntent.putExtra("bookId", bookId);
                    setResult(RESULT_OK, resultIntent);
                } else {
                    Toast.makeText(BookDetailsActivity.this, "Failed to add book to shelf", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddToShelfResponse> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
