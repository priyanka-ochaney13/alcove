package com.alcove;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        bookCover = findViewById(R.id.bookCover);
        bookTitle = findViewById(R.id.bookTitle);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookRating = findViewById(R.id.bookRating);
        ratingCount = findViewById(R.id.ratingCount);
        description = findViewById(R.id.description);
        addToShelfButton = findViewById(R.id.addToShelfButton);
        backButton = findViewById(R.id.backButton);
        reviewsRecycler = findViewById(R.id.reviewsRecycler);

        // Get intent data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bookTitle.setText(extras.getString("bookTitle", "Unknown"));
            bookAuthor.setText(extras.getString("bookAuthor", "Unknown"));
            bookRating.setRating(extras.getFloat("bookRating", 0));
            ratingCount.setText("(" + extras.getInt("ratingCount", 0) + " ratings)");
        }

        // Set dummy description
        String bookId = extras != null ? String.valueOf(extras.getInt("bookId", 1)) : "1";
        description.setText(getBookDescription(Integer.parseInt(bookId)));

        // Setup reviews
        setupReviews();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Add to shelf button
        addToShelfButton.setOnClickListener(v -> {
            Toast.makeText(BookDetailsActivity.this, "Added to your bookshelf!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupReviews() {
        List<Review> reviews = getDummyReviews();
        reviewAdapter = new ReviewAdapter(this, reviews);
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.setAdapter(reviewAdapter);
    }

    private List<Review> getDummyReviews() {
        return DummyDataGenerator.getBookReviews();
    }

    private String getBookDescription(int bookId) {
        switch (bookId) {
            case 1:
                return "F. Scott Fitzgerald's masterpiece, The Great Gatsby, is a timeless novel that explores themes of love, ambition, wealth, and the American Dream. Set in the Jazz Age of the 1920s, the story follows Nick Carraway as he becomes entangled in the world of the mysterious millionaire Jay Gatsby. With its lyrical prose and unforgettable characters, this novel remains one of the greatest works in American literature.";
            case 2:
                return "Harper Lee's To Kill a Mockingbird is a powerful story about racial injustice and childhood innocence in the American South. Through the eyes of Scout Finch, readers experience a deeply moving narrative about her father's defense of a Black man falsely accused of a crime. This Pulitzer Prize-winning novel continues to resonate with readers of all ages.";
            case 3:
                return "George Orwell's dystopian masterpiece, 1984, presents a terrifying vision of totalitarianism. The novel follows Winston Smith as he attempts to rebel against the oppressive regime of Big Brother. With its chilling exploration of surveillance, propaganda, and control, this novel serves as a warning about the dangers of authoritarianism.";
            case 6:
                return "It Ends with Us by Colleen Hoover is a gripping contemporary romance that explores domestic violence and the cycle of abuse. Following Lily Bloom as she opens her flower shop and finds herself entangled in a complex love triangle, this novel tackles difficult themes while delivering an emotionally resonant story about healing and self-discovery.";
            case 7:
                return "Fourth Wing by Rebecca Yarros is an epic fantasy romance set in a brutal war college for dragon riders. The story follows Violet Sorrengail as she navigates dangerous politics, intense training, and an unexpected romance. With richly developed characters and immersive worldbuilding, this book has captivated readers worldwide.";
            default:
                return "This is a captivating book that explores profound themes and resonates with readers through its compelling storytelling and memorable characters. A must-read that will leave you thinking long after you finish the last page.";
        }
    }
}
