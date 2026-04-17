package com.alcove;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * MyBookshelfActivity - Demonstrates Fragment usage with swipeable tabs using ViewPager2
 * Swipe gestures allow switching between Currently Reading, Want to Read, and Read tabs
 */
public class MyBookshelfActivity extends AppCompatActivity {

    private ImageView backButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_my_bookshelf);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Setup ViewPager2 with adapter
        BookshelfPagerAdapter adapter = new BookshelfPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach TabLayout to ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Currently Reading");
                    break;
                case 1:
                    tab.setText("Want to Read");
                    break;
                case 2:
                    tab.setText("Read");
                    break;
            }
        }).attach();

        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Adapter for ViewPager2 to manage fragments
     */
    private static class BookshelfPagerAdapter extends FragmentStateAdapter {

        public BookshelfPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return BookshelfFragment.newInstance(BookshelfFragment.TAB_CURRENTLY_READING);
                case 1:
                    return BookshelfFragment.newInstance(BookshelfFragment.TAB_WANT_TO_READ);
                case 2:
                    return BookshelfFragment.newInstance(BookshelfFragment.TAB_READ);
                default:
                    return BookshelfFragment.newInstance(BookshelfFragment.TAB_CURRENTLY_READING);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
