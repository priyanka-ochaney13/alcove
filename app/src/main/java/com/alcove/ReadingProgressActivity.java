package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.Book;
import com.alcove.models.ReadingProgressResponse;
import com.alcove.models.ReadingProgressUpdateRequest;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingProgressActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";
    public static final String EXTRA_BOOK_TITLE = "book_title";
    public static final String EXTRA_BOOK_AUTHOR = "book_author";
    public static final String EXTRA_BOOK_IMAGE_URL = "book_image_url";

    private int bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;

    private ImageView bookCover;
    private TextView bookTitleText;
    private TextView bookAuthorText;
    private ProgressBar progressBar;
    private TextView progressText;
    private EditText currentPageEdit;
    private EditText totalPagesEdit;
    private CheckBox completedCheckBox;
    private Button updateProgressBtn;
    private TextView startDateText;
    private TextView daysReadingText;

    private ReadingProgressResponse currentProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_progress);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get intent data
        extractIntentData();

        // Initialize views
        initializeViews();

        // Load current progress
        loadReadingProgress();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        bookId = intent.getIntExtra(EXTRA_BOOK_ID, -1);
        bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE);
        bookAuthor = intent.getStringExtra(EXTRA_BOOK_AUTHOR);
        bookImageUrl = intent.getStringExtra(EXTRA_BOOK_IMAGE_URL);

        if (bookId == -1) {
            Toast.makeText(this, "Invalid book", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initializeViews() {
        bookCover = findViewById(R.id.bookCover);
        bookTitleText = findViewById(R.id.bookTitle);
        bookAuthorText = findViewById(R.id.bookAuthor);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        currentPageEdit = findViewById(R.id.currentPageEdit);
        totalPagesEdit = findViewById(R.id.totalPagesEdit);
        completedCheckBox = findViewById(R.id.completedCheckBox);
        updateProgressBtn = findViewById(R.id.updateProgressBtn);
        startDateText = findViewById(R.id.startDateText);
        daysReadingText = findViewById(R.id.daysReadingText);

        // Set book info
        bookTitleText.setText(bookTitle != null ? bookTitle : "Unknown Book");
        bookAuthorText.setText(bookAuthor != null ? bookAuthor : "Unknown Author");

        if (bookImageUrl != null && !bookImageUrl.isEmpty()) {
            Glide.with(this)
                .load(bookImageUrl)
                .placeholder(R.drawable.ic_book_placeholder)
                .error(R.drawable.ic_book_placeholder)
                .into(bookCover);
        }

        // Setup button click listener
        updateProgressBtn.setOnClickListener(v -> updateReadingProgress());

        // Setup checkbox listener
        completedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Auto-fill current page with total pages if available
                String totalPagesStr = totalPagesEdit.getText().toString().trim();
                if (!totalPagesStr.isEmpty() && currentPageEdit.getText().toString().trim().isEmpty()) {
                    currentPageEdit.setText(totalPagesStr);
                }
            }
        });
    }

    private void loadReadingProgress() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getBookReadingProgress(bookId).enqueue(new Callback<ReadingProgressResponse>() {
            @Override
            public void onResponse(Call<ReadingProgressResponse> call, Response<ReadingProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProgress = response.body();
                    updateUIWithProgress(currentProgress);
                }
            }

            @Override
            public void onFailure(Call<ReadingProgressResponse> call, Throwable t) {
                Toast.makeText(ReadingProgressActivity.this, "Failed to load progress", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithProgress(ReadingProgressResponse progress) {
        // Update progress bar and text
        updateProgressDisplay(progress.getCurrentPage(), progress.getTotalPages());

        // Update form fields
        currentPageEdit.setText(String.valueOf(progress.getCurrentPage()));
        if (progress.getTotalPages() != null) {
            totalPagesEdit.setText(String.valueOf(progress.getTotalPages()));
        }
        completedCheckBox.setChecked(progress.isCompleted());

        // Update reading session info
        if (progress.getStartDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            startDateText.setText("Started: " + sdf.format(progress.getStartDate()));

            // Calculate days reading
            long diffInMillis = System.currentTimeMillis() - progress.getStartDate().getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            daysReadingText.setText("Days reading: " + days);
        } else {
            startDateText.setText("Started: Not set");
            daysReadingText.setText("Days reading: 0");
        }
    }

    private void updateProgressDisplay(int currentPage, Integer totalPages) {
        if (totalPages != null && totalPages > 0) {
            int progressPercent = (int) ((currentPage * 100.0) / totalPages);
            progressBar.setProgress(Math.min(progressPercent, 100));
            progressText.setText(currentPage + " / " + totalPages + " pages (" + progressPercent + "%)");
        } else {
            progressBar.setProgress(0);
            progressText.setText(currentPage + " pages read");
        }
    }

    private void updateReadingProgress() {
        // Validate input
        String currentPageStr = currentPageEdit.getText().toString().trim();
        if (currentPageStr.isEmpty()) {
            Toast.makeText(this, "Please enter current page", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentPage;
        try {
            currentPage = Integer.parseInt(currentPageStr);
            if (currentPage < 0) {
                Toast.makeText(this, "Current page cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid current page number", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer totalPages = null;
        String totalPagesStr = totalPagesEdit.getText().toString().trim();
        if (!totalPagesStr.isEmpty()) {
            try {
                totalPages = Integer.parseInt(totalPagesStr);
                if (totalPages <= 0) {
                    Toast.makeText(this, "Total pages must be positive", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentPage > totalPages) {
                    Toast.makeText(this, "Current page cannot exceed total pages", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid total pages number", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean isCompleted = completedCheckBox.isChecked();

        // Create update request
        ReadingProgressUpdateRequest request = new ReadingProgressUpdateRequest();
        request.setCurrentPage(currentPage);
        request.setTotalPages(totalPages);
        request.setIsCompleted(isCompleted);

        // If this is the first time updating and we don't have a start date, set it
        if (currentProgress == null || currentProgress.getId() == 0) {
            request.setStartDate(new Date());
        }

        // If marking as completed and no end date set, set it
        if (isCompleted && (currentProgress == null || currentProgress.getEndDate() == null)) {
            request.setEndDate(new Date());
        }

        // Send update request
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.updateReadingProgress(bookId, request).enqueue(new Callback<ReadingProgressResponse>() {
            @Override
            public void onResponse(Call<ReadingProgressResponse> call, Response<ReadingProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProgress = response.body();
                    updateUIWithProgress(currentProgress);
                    Toast.makeText(ReadingProgressActivity.this, "Progress updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReadingProgressActivity.this, "Failed to update progress", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReadingProgressResponse> call, Throwable t) {
                Toast.makeText(ReadingProgressActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Intent createIntent(android.content.Context context, Book book) {
        Intent intent = new Intent(context, ReadingProgressActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, book.getId());
        intent.putExtra(EXTRA_BOOK_TITLE, book.getTitle());
        intent.putExtra(EXTRA_BOOK_AUTHOR, book.getAuthor());
        intent.putExtra(EXTRA_BOOK_IMAGE_URL, book.getImageUrl());
        return intent;
    }
}
