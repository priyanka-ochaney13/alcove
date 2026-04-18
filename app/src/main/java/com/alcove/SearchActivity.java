package com.alcove;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.BookResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView searchResultsRecycler;
    private ImageView backButton;
    private BookAdapter bookAdapter;
    private List<Book> resultsList = new ArrayList<>();
    private Call<List<BookResponse>> currentSearchCall;

    // Filter UI elements
    private Button filterToggleButton;
    private LinearLayout filterPanel;
    private EditText authorFilter;
    private EditText genreFilter;
    private EditText minRatingFilter;
    private EditText maxRatingFilter;
    private EditText yearFilter;
    private EditText isbnFilter;
    private Button applyFiltersButton;

    private boolean filtersVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        searchInput = findViewById(R.id.searchInput);
        searchResultsRecycler = findViewById(R.id.searchResultsRecycler);
        backButton = findViewById(R.id.backButton);
        filterToggleButton = findViewById(R.id.filterToggleButton);
        filterPanel = findViewById(R.id.filterPanel);
        authorFilter = findViewById(R.id.authorFilter);
        genreFilter = findViewById(R.id.genreFilter);
        minRatingFilter = findViewById(R.id.minRatingFilter);
        maxRatingFilter = findViewById(R.id.maxRatingFilter);
        yearFilter = findViewById(R.id.yearFilter);
        isbnFilter = findViewById(R.id.isbnFilter);
        applyFiltersButton = findViewById(R.id.applyFiltersButton);

        // Setup RecyclerView with grid layout
        setupSearchResults();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Search functionality
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearchWithCurrentFilters(query);
            }
            return false;
        });

        // Filter toggle button
        filterToggleButton.setOnClickListener(v -> toggleFilterPanel());

        // Apply filters
        applyFiltersButton.setOnClickListener(v -> applyFilters());
    }

    private void setupSearchResults() {
        bookAdapter = new BookAdapter(this, resultsList);
        searchResultsRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecycler.setAdapter(bookAdapter);
    }

    private void performSearch(String query, String author, String genre, Float minRating, Float maxRating, Integer year, String isbn) {
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        currentSearchCall = apiService.searchBooks(query, author, genre, minRating, maxRating, year, isbn, 50);
        currentSearchCall.enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                try {
                    if (isFinishing() || isDestroyed() || bookAdapter == null) {
                        return;
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        resultsList.clear();
                        for (BookResponse b : response.body()) {
                            resultsList.add(new Book(b.getId(), b.getTitle(), "", b.getAverageRating(), 0, b.getImageUrl()));
                        }
                        if (bookAdapter != null) {
                            bookAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    Log.e("SearchActivity", "Error processing search response", e);
                }
            }

            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e("SearchActivity", "Search failed: " + t.getMessage());
            }
        });
    }

    private void performSearch(String query) {
        performSearch(query, null, null, null, null, null, null);
    }

    private void toggleFilterPanel() {
        filtersVisible = !filtersVisible;
        if (filtersVisible) {
            filterPanel.setVisibility(View.VISIBLE);
            filterToggleButton.setText("Hide Filters");
        } else {
            filterPanel.setVisibility(View.GONE);
            filterToggleButton.setText("Show Filters");
            // If we hide the filters, we might want to refresh the search if filters were active
            String query = searchInput.getText().toString().trim();
            performSearchWithCurrentFilters(query);
        }
    }

    private void applyFilters() {
        String query = searchInput.getText().toString().trim();
        performSearchWithCurrentFilters(query);
        toggleFilterPanel();
    }

    private void performSearchWithCurrentFilters(String query) {
        String author = authorFilter.getText().toString().trim();
        if (author.isEmpty()) author = null;
        
        String genre = genreFilter.getText().toString().trim();
        if (genre.isEmpty()) genre = null;
        
        Float minRating = null;
        Float maxRating = null;
        Integer year = null;
        String isbn = null;

        try {
            String minRatingStr = minRatingFilter.getText().toString().trim();
            if (!minRatingStr.isEmpty()) {
                minRating = Float.parseFloat(minRatingStr);
            }
            
            String maxRatingStr = maxRatingFilter.getText().toString().trim();
            if (!maxRatingStr.isEmpty()) {
                maxRating = Float.parseFloat(maxRatingStr);
            }
            
            String yearStr = yearFilter.getText().toString().trim();
            if (!yearStr.isEmpty()) {
                year = Integer.parseInt(yearStr);
            }
            
            isbn = isbnFilter.getText().toString().trim();
            if (isbn.isEmpty()) isbn = null;
            
        } catch (NumberFormatException e) {
            Log.e("SearchActivity", "Error parsing filter values", e);
        }

        Log.d("SearchActivity", "Performing search with q='" + query + "', author='" + author + "', genre='" + genre + "', minRating=" + minRating + ", maxRating=" + maxRating + ", year=" + year + ", isbn=" + isbn);
        performSearch(query, author, genre, minRating, maxRating, year, isbn);
    }

    @Override
    protected void onDestroy() {
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
            currentSearchCall = null;
        }
        super.onDestroy();
    }
}
