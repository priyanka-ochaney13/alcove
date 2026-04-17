package com.alcove;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder> {

    private Context context;
    private List<MyReviewItem> reviews;

    public MyReviewAdapter(Context context, List<MyReviewItem> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_review, parent, false);
        return new MyReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        MyReviewItem review = reviews.get(position);
        holder.bookTitle.setText(review.getBookTitle());

        if (review.getBookAuthor() != null) {
            holder.bookAuthor.setText(review.getBookAuthor());
            holder.bookAuthor.setVisibility(View.VISIBLE);
        } else {
            holder.bookAuthor.setVisibility(View.GONE);
        }

        holder.ratingBar.setRating(review.getRating());

        if (review.getReviewText() != null) {
            holder.reviewText.setText(review.getReviewText());
            holder.reviewText.setVisibility(View.VISIBLE);
        } else {
            holder.reviewText.setVisibility(View.GONE);
        }

        if (holder.bookCover != null) {
            Glide.with(context)
                .load(review.getBookCoverUrl())
                .placeholder(R.drawable.rounded_button_background)
                .error(R.drawable.rounded_button_background)
                .into(holder.bookCover);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class MyReviewViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        TextView bookAuthor;
        RatingBar ratingBar;
        TextView reviewText;
        ImageView deleteBtn;
        ImageView bookCover;

        public MyReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            reviewText = itemView.findViewById(R.id.reviewText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            bookCover = itemView.findViewById(R.id.bookCover);
        }
    }
}
