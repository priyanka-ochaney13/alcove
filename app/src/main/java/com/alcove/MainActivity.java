package com.alcove;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

/**
 * MainActivity - Demonstrates Fragment usage for modular UI sections and gesture handling
 * Fling gestures on scroll view for fast navigation
 */
public class MainActivity extends AppCompatActivity {

    private ImageView profileIcon;
    private ImageView searchIcon;
    private ImageView bookshelfIcon;
    private ScrollView scrollView;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        profileIcon = findViewById(R.id.profileIcon);
        searchIcon = findViewById(R.id.searchIcon);
        bookshelfIcon = findViewById(R.id.bookshelfIcon);
        scrollView = findViewById(R.id.scrollView);

        // Load fragments for each section
        if (savedInstanceState == null) {
            loadBookSections();
        }

        // Setup navigation intents
        setupNavigation();

        // Setup fling gesture for fast scrolling
        setupFlingGesture();
    }

    /**
     * Load all book section fragments
     * Demonstrates adding multiple fragments to different containers
     */
    private void loadBookSections() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Add Popular Books Fragment
        BooksListFragment popularFragment = BooksListFragment.newInstance(
                BooksListFragment.CATEGORY_POPULAR
        );
        transaction.add(R.id.popularBooksContainer, popularFragment);

        // Add Trending Books Fragment
        BooksListFragment trendingFragment = BooksListFragment.newInstance(
                BooksListFragment.CATEGORY_TRENDING
        );
        transaction.add(R.id.trendingBooksContainer, trendingFragment);

        // Add Recommended Books Fragment
        BooksListFragment recommendedFragment = BooksListFragment.newInstance(
                BooksListFragment.CATEGORY_RECOMMENDED
        );
        transaction.add(R.id.recommendedBooksContainer, recommendedFragment);

        transaction.commitAllowingStateLoss();
    }

    /**
     * Setup navigation using Intents
     * Demonstrates Intent usage for screen navigation
     */
    private void setupNavigation() {
        // Search icon - Navigate to SearchActivity
        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Bookshelf icon - Navigate to MyBookshelfActivity
        bookshelfIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyBookshelfActivity.class);
            startActivity(intent);
        });

        // Profile icon - Navigate to UserProfileActivity
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Setup fling gesture detection on scroll view
     * Enables fast navigation between book sections
     */
    private void setupFlingGesture() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown();
                    } else {
                        onSwipeUp();
                    }
                    return true;
                }
                return false;
            }
        });

        scrollView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    /**
     * Handle swipe down gesture
     * Scrolls to top of the page
     */
    private void onSwipeDown() {
        scrollView.smoothScrollTo(0, 0);
    }

    /**
     * Handle swipe up gesture
     * Scrolls to bottom of the page
     */
    private void onSwipeUp() {
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
