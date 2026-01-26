package com.alcove;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcove.dummy.DummyDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class MyReviewsActivity extends AppCompatActivity {

    private RecyclerView myReviewsRecycler;
    private ImageView backButton;
    private MyReviewAdapter myReviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_reviews);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        myReviewsRecycler = findViewById(R.id.myReviewsRecycler);
        backButton = findViewById(R.id.backButton);

        // Setup reviews
        setupMyReviews();

        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    private void setupMyReviews() {
        List<MyReviewItem> reviews = getDummyReviews();
        myReviewAdapter = new MyReviewAdapter(this, reviews);
        myReviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        myReviewsRecycler.setAdapter(myReviewAdapter);
    }

    private List<MyReviewItem> getDummyReviews() {
        return DummyDataGenerator.getUserReviews();
    }
}
