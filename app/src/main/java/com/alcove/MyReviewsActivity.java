package com.alcove;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReviewsActivity extends AppCompatActivity {

    private RecyclerView myReviewsRecycler;
    private ImageView backButton;
    private MyReviewAdapter myReviewAdapter;
    private List<MyReviewItem> reviewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
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
        myReviewAdapter = new MyReviewAdapter(this, reviewsList);
        myReviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        myReviewsRecycler.setAdapter(myReviewAdapter);
        fetchMyReviews();
    }

    private void fetchMyReviews() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserReviews().enqueue(new Callback<List<com.alcove.models.ReviewResponse>>() {
            @Override
            public void onResponse(Call<List<com.alcove.models.ReviewResponse>> call, Response<List<com.alcove.models.ReviewResponse>> response) {
                try {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        reviewsList.clear();
                        for (com.alcove.models.ReviewResponse r : response.body()) {
                            String bookTitle = "Unknown Book";
                            String bookCover = null;
                            if (r.getBook() != null) {
                                bookTitle = r.getBook().getTitle() != null ? r.getBook().getTitle() : "Unknown Book";
                                bookCover = r.getBook().getImageUrl();
                            }
                            int rating = r.getRating() != null ? r.getRating().intValue() : 0;
                            reviewsList.add(new MyReviewItem(bookTitle, bookCover, rating, r.getContent(), "Just now"));
                        }
                        if (myReviewAdapter != null) {
                            myReviewAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    Log.e("MyReviewsActivity", "Error processing reviews response", e);
                }
            }

            @Override
            public void onFailure(Call<List<com.alcove.models.ReviewResponse>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e("MyReviewsActivity", "Failed to fetch user reviews", t);
            }
        });
    }
}
