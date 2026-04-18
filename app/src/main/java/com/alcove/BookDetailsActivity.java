package com.alcove;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ProgressBar;

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
    public static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";

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
    private androidx.cardview.widget.CardView progressCard;
    private ProgressBar bookDetailsProgressBar;
    private TextView bookDetailsProgressText;
    private Button updateProgressShortcutBtn;

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
        progressCard = findViewById(R.id.progressCard);
        bookDetailsProgressBar = findViewById(R.id.bookDetailsProgressBar);
        bookDetailsProgressText = findViewById(R.id.bookDetailsProgressText);
        updateProgressShortcutBtn = findViewById(R.id.updateProgressShortcutBtn);
    }

    /**
     * Extract data passed via Intent
     * Demonstrates proper Intent data extraction with default values
     */
    private void extractIntentData() {
        Intent intent = getIntent();

        bookId = intent.getIntExtra(EXTRA_BOOK_ID, -1);
        android.util.Log.d("BookDetailsActivity", "Extracting intent data for bookId: " + bookId);
        if (bookId <= 0) {
            Toast.makeText(this, "Invalid book details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String title = intent.getStringExtra(EXTRA_BOOK_TITLE);
        String author = intent.getStringExtra(EXTRA_BOOK_AUTHOR);
        float rating = intent.getFloatExtra(EXTRA_BOOK_RATING, 0.0f);
        int reviewsCount = intent.getIntExtra(EXTRA_RATING_COUNT, 0);
        String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (bookId > 0) {
            fetchReadingProgress();
        }
    }

    private void fetchReadingProgress() {
        ApiClient.getClient().create(ApiService.class).getBookReadingProgress(bookId).enqueue(new Callback<com.alcove.models.ReadingProgressResponse>() {
            @Override
            public void onResponse(Call<com.alcove.models.ReadingProgressResponse> call, Response<com.alcove.models.ReadingProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.alcove.models.ReadingProgressResponse progress = response.body();
                    progressCard.setVisibility(android.view.View.VISIBLE);

                    int current = progress.getCurrentPage();
                    Integer total = progress.getTotalPages();

                    if (total != null && total > 0) {
                        int percent = (int) ((current * 100.0) / total);
                        bookDetailsProgressBar.setProgress(Math.min(percent, 100));
                        bookDetailsProgressText.setText(current + " / " + total + " pages (" + percent + "%)");
                    } else {
                        bookDetailsProgressBar.setProgress(0);
                        bookDetailsProgressText.setText(current + " pages read");
                    }

                    updateProgressShortcutBtn.setOnClickListener(v -> {
                        Intent intent = new Intent(BookDetailsActivity.this, ReadingProgressActivity.class);
                        intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_ID, bookId);
                        intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_TITLE, bookTitle.getText().toString());
                        intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_AUTHOR, bookAuthor.getText().toString());
                        // try to find original book object or just pass what we have
                        startActivity(intent);
                    });
                } else {
                    progressCard.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.alcove.models.ReadingProgressResponse> call, Throwable t) {
                progressCard.setVisibility(android.view.View.GONE);
            }
        });
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
                        android.util.Log.d("BookDetailsActivity", "Successfully loaded book: " + book.getTitle());

                        if (bookTitle != null) bookTitle.setText(book.getTitle());
                        if (bookAuthor != null) {
                            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                                StringBuilder authors = new StringBuilder();
                                for (int i = 0; i < book.getAuthors().size(); i++) {
                                    authors.append(book.getAuthors().get(i).getName());
                                    if (i < book.getAuthors().size() - 1) authors.append(", ");
                                }
                                bookAuthor.setText(authors.toString());
                            } else {
                                bookAuthor.setText("Unknown Author");
                            }
                        }
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

                        if (book.getReviews() != null && reviewsRecycler != null) {
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

                        // Update FAB or shortcut link with actual data
                        updateProgressShortcutBtn.setOnClickListener(v -> {
                            Intent intent = new Intent(BookDetailsActivity.this, ReadingProgressActivity.class);
                            intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_ID, bookId);
                            intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_TITLE, book.getTitle());

                            String authorsStr = "Unknown Author";
                            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                                authorsStr = book.getAuthors().get(0).getName();
                            }
                            intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_AUTHOR, authorsStr);
                            intent.putExtra(ReadingProgressActivity.EXTRA_BOOK_IMAGE_URL, book.getImageUrl());
                            startActivity(intent);
                        });

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

    private void refreshShelfButtonState() {
        ApiClient.getService().getBookShelfStatus(bookId).enqueue(new Callback<ShelfStatusResponse>() {
            @Override
            public void onResponse(Call<ShelfStatusResponse> call, Response<ShelfStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getShelf() != null) {
                    addToShelfButton.setText("On Shelf: " + response.body().getShelf());
                } else {
                    addToShelfButton.setText("Add to Shelf");
                }
            }
            @Override
            public void onFailure(Call<ShelfStatusResponse> call, Throwable t) {}
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
        addToShelfButton.setOnClickListener(v -> showShelfSelectionDialog());

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
        addBookToShelf(shelfId, null);
    }

    private void addBookToShelf(int shelfId, String shelfName) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        AddBookToShelfRequest request = new AddBookToShelfRequest(bookId);
        apiService.addBookToShelf(shelfId, request).enqueue(new Callback<AddToShelfResponse>() {
            @Override
            public void onResponse(Call<AddToShelfResponse> call, Response<AddToShelfResponse> response) {
                if (response.isSuccessful()) {
                    String message = shelfName != null ? "Added to '" + shelfName + "' shelf!" : "Added to shelf!";
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
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

    private void removeBookFromShelf(int shelfId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.removeBookFromShelf(shelfId, bookId).enqueue(new Callback<AddToShelfResponse>() {
            @Override
            public void onResponse(Call<AddToShelfResponse> call, Response<AddToShelfResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailsActivity.this, "Removed from shelf", Toast.LENGTH_SHORT).show();
                    refreshShelfButtonState();
                    // Pass result back
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("bookRemoved", true);
                    resultIntent.putExtra("bookId", bookId);
                    setResult(RESULT_OK, resultIntent);
                } else {
                    Toast.makeText(BookDetailsActivity.this, "Failed to remove book from shelf", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddToShelfResponse> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show a dialog to select shelf for the book
     */
    private void showShelfSelectionDialog() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // First get current shelf status
        apiService.getBookShelfStatus(bookId).enqueue(new Callback<ShelfStatusResponse>() {
            @Override
            public void onResponse(Call<ShelfStatusResponse> call, Response<ShelfStatusResponse> response) {
                final String currentShelfName;
                final Integer currentShelfId;
                if (response.isSuccessful() && response.body() != null) {
                    currentShelfName = response.body().getShelf();
                    currentShelfId = response.body().getShelfId();
                } else {
                    currentShelfName = null;
                    currentShelfId = null;
                }

                // Now get user shelves
                apiService.getUserShelves().enqueue(new Callback<List<CustomShelfResponse>>() {
                    @Override
                    public void onResponse(Call<List<CustomShelfResponse>> call, Response<List<CustomShelfResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<CustomShelfResponse> shelves = response.body();

                            // Create list of options
                            List<String> options = new ArrayList<>();
                            List<String> shelfNames = new ArrayList<>();
                            List<Integer> shelfIds = new ArrayList<>();

                            // Add shelves
                            int checkedItem = -1;
                            for (int i = 0; i < shelves.size(); i++) {
                                CustomShelfResponse shelf = shelves.get(i);
                                String displayName = shelf.getName();
                                if (currentShelfName != null && currentShelfName.equals(shelf.getName())) {
                                    displayName += " (current)";
                                    checkedItem = i;
                                }
                                options.add(displayName);
                                shelfNames.add(shelf.getName());
                                shelfIds.add(shelf.getId());
                            }

                            // Add remove option
                            options.add("Remove from all shelves");
                            shelfNames.add(null); // No name for remove
                            shelfIds.add(-1); // Use -1 to indicate remove

                            // Show single choice dialog
                            new AlertDialog.Builder(BookDetailsActivity.this)
                                .setTitle("Select Shelf")
                                .setSingleChoiceItems(options.toArray(new String[0]), checkedItem, (dialog, which) -> {
                                    // Handle selection
                                    if (which == shelves.size()) {
                                        // Remove selected
                                        if (currentShelfId != null) {
                                            removeBookFromShelf(currentShelfId);
                                        } else {
                                            Toast.makeText(BookDetailsActivity.this, "Book is not in any shelf", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Shelf selected
                                        int selectedShelfId = shelfIds.get(which);
                                        String selectedShelfName = shelfNames.get(which);
                                        if (currentShelfId != null && currentShelfId.equals(selectedShelfId)) {
                                            // Same shelf, do nothing
                                            Toast.makeText(BookDetailsActivity.this, "Book is already in this shelf", Toast.LENGTH_SHORT).show();
                                        } else {
                                            addBookToShelf(selectedShelfId, selectedShelfName);
                                        }
                                    }
                                    dialog.dismiss();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
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

            @Override
            public void onFailure(Call<ShelfStatusResponse> call, Throwable t) {
                // On failure, still show dialog without current info
                showShelfSelectionDialogWithoutCurrent();
            }
        });
    }

    private void showShelfSelectionDialogWithoutCurrent() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserShelves().enqueue(new Callback<List<CustomShelfResponse>>() {
            @Override
            public void onResponse(Call<List<CustomShelfResponse>> call, Response<List<CustomShelfResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CustomShelfResponse> shelves = response.body();

                    List<String> options = new ArrayList<>();
                    for (CustomShelfResponse shelf : shelves) {
                        options.add(shelf.getName());
                    }
                    options.add("Remove from all shelves");

                    new AlertDialog.Builder(BookDetailsActivity.this)
                        .setTitle("Select Shelf")
                        .setItems(options.toArray(new String[0]), (dialog, which) -> {
                            if (which == shelves.size()) {
                                // Remove
                                Toast.makeText(BookDetailsActivity.this, "Book is not in any shelf", Toast.LENGTH_SHORT).show();
                            } else {
                                addBookToShelf(shelves.get(which).getId());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
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
}
