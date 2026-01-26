package com.alcove;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyBookshelfActivity extends AppCompatActivity {

    private Button currentlyReadingBtn;
    private Button wantToReadBtn;
    private Button readBtn;
    private RecyclerView bookshelfRecycler;
    private ImageView backButton;
    private BookAdapter bookAdapter;
    private int currentTab = 0; // 0: Currently Reading, 1: Want to Read, 2: Read

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_bookshelf);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        currentlyReadingBtn = findViewById(R.id.currentlyReadingBtn);
        wantToReadBtn = findViewById(R.id.wantToReadBtn);
        readBtn = findViewById(R.id.readBtn);
        bookshelfRecycler = findViewById(R.id.bookshelfRecycler);
        backButton = findViewById(R.id.backButton);

        // Setup initial view
        displayBooks(0);

        // Tab buttons
        currentlyReadingBtn.setOnClickListener(v -> {
            currentTab = 0;
            updateTabButtons();
            displayBooks(0);
        });

        wantToReadBtn.setOnClickListener(v -> {
            currentTab = 1;
            updateTabButtons();
            displayBooks(1);
        });

        readBtn.setOnClickListener(v -> {
            currentTab = 2;
            updateTabButtons();
            displayBooks(2);
        });

        // Back button
        backButton.setOnClickListener(v -> finish());

        updateTabButtons();
    }

    private void displayBooks(int tab) {
        List<Book> books = getDummyBooks(tab);
        bookAdapter = new BookAdapter(this, books);
        bookshelfRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        bookshelfRecycler.setAdapter(bookAdapter);
    }

    private void updateTabButtons() {
        currentlyReadingBtn.setBackgroundColor(currentTab == 0 ? getResources().getColor(android.R.color.darker_gray) : getResources().getColor(android.R.color.white));
        wantToReadBtn.setBackgroundColor(currentTab == 1 ? getResources().getColor(android.R.color.darker_gray) : getResources().getColor(android.R.color.white));
        readBtn.setBackgroundColor(currentTab == 2 ? getResources().getColor(android.R.color.darker_gray) : getResources().getColor(android.R.color.white));
    }

    private List<Book> getDummyBooks(int tab) {
        if (tab == 0) {
            return DummyDataGenerator.getCurrentlyReading();
        } else if (tab == 1) {
            return DummyDataGenerator.getWantToRead();
        } else {
            return DummyDataGenerator.getAlreadyRead();
        }
    }
}
