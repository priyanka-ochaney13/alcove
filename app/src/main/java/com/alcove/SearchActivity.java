package com.alcove;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView searchResultsRecycler;
    private ImageView backButton;
    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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

        // Setup RecyclerView with grid layout
        setupSearchResults();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Search functionality
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString();
            if (!query.isEmpty()) {
                performSearch(query);
            }
            return false;
        });
    }

    private void setupSearchResults() {
        List<Book> results = getDummySearchResults();
        bookAdapter = new BookAdapter(this, results);
        searchResultsRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecycler.setAdapter(bookAdapter);
    }

    private void performSearch(String query) {
        // TODO: Call backend API with search query
        setupSearchResults();
    }

    private List<Book> getDummySearchResults() {
        return DummyDataGenerator.getSearchResults();
    }
}
