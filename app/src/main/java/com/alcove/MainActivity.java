package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView popularBooksRecycler;
    private RecyclerView trendingBooksRecycler;
    private RecyclerView recommendedBooksRecycler;
    private ImageView profileIcon;
    private ImageView searchIcon;
    private ImageView bookshelfIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        popularBooksRecycler = findViewById(R.id.popularBooksRecycler);
        trendingBooksRecycler = findViewById(R.id.trendingBooksRecycler);
        recommendedBooksRecycler = findViewById(R.id.recommendedBooksRecycler);
        profileIcon = findViewById(R.id.profileIcon);
        searchIcon = findViewById(R.id.searchIcon);
        bookshelfIcon = findViewById(R.id.bookshelfIcon);

        // Setup RecyclerViews
        setupRecyclerViews();

        // Search icon click listener
        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Bookshelf icon click listener
        bookshelfIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyBookshelfActivity.class);
            startActivity(intent);
        });

        // Profile icon click listener
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        // Setup Popular Books
        List<Book> popularBooks = DummyDataGenerator.getPopularBooks();
        BookAdapter popularAdapter = new BookAdapter(this, popularBooks);
        popularBooksRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        popularBooksRecycler.setAdapter(popularAdapter);

        // Setup Trending Books
        List<Book> trendingBooks = DummyDataGenerator.getTrendingBooks();
        BookAdapter trendingAdapter = new BookAdapter(this, trendingBooks);
        trendingBooksRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trendingBooksRecycler.setAdapter(trendingAdapter);

        // Setup Recommended Books
        List<Book> recommendedBooks = DummyDataGenerator.getRecommendedBooks();
        BookAdapter recommendedAdapter = new BookAdapter(this, recommendedBooks);
        recommendedBooksRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedBooksRecycler.setAdapter(recommendedAdapter);
    }

    private List<Book> getDummyBooks(String category) {
        // This method is no longer used - using DummyDataGenerator instead
        return new ArrayList<>();
    }
}