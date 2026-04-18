package com.alcove;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.AddToShelfResponse;
import com.alcove.models.CreateReviewRequest;
import com.alcove.models.ReviewResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviews;
    private int bookId;

    public ReviewAdapter(Context context, List<Review> reviews, int bookId) {
        this.context = context;
        this.reviews = reviews;
        this.bookId = bookId;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.userName.setText(review.getUserName());
        holder.ratingBar.setRating(review.getRating());
        holder.reviewText.setText(review.getReviewText());

        // Check if current user owns this review
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isOwner = currentUser != null && currentUser.getUid().equals(review.getUserId());

        // Show/hide action buttons based on ownership
        holder.actionButtons.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (isOwner) {
            holder.editBtn.setOnClickListener(v -> showEditReviewDialog(review, position));
            holder.deleteBtn.setOnClickListener(v -> showDeleteConfirmationDialog(review, position));
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private void showEditReviewDialog(Review review, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Review");

        // Create the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_review, null);
        EditText reviewContentEdit = dialogView.findViewById(R.id.reviewContentEdit);
        RatingBar ratingBarEdit = dialogView.findViewById(R.id.ratingBarEdit);

        // Pre-fill with existing data
        reviewContentEdit.setText(review.getReviewText());
        ratingBarEdit.setRating(review.getRating());

        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newContent = reviewContentEdit.getText().toString().trim();
            int newRating = (int) ratingBarEdit.getRating();

            if (newContent.isEmpty()) {
                Toast.makeText(context, "Please enter review content", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create update request
            CreateReviewRequest request = new CreateReviewRequest();
            request.setContent(newContent);
            request.setRating(newRating);

            // Call API to update review
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.updateReview(bookId, request).enqueue(new Callback<ReviewResponse>() {
                @Override
                public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                    if (response.isSuccessful()) {
                        // Update local review
                        review.setReviewText(newContent);
                        review.setRating(newRating);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Review updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update review", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ReviewResponse> call, Throwable t) {
                    Toast.makeText(context, "Network error while updating review: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(Review review, int position) {
        int positionToRemove = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Review");
        builder.setMessage("Are you sure you want to delete this review?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Call API to delete review
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.deleteReview(bookId).enqueue(new Callback<AddToShelfResponse>() {
                @Override
                public void onResponse(Call<AddToShelfResponse> call, Response<AddToShelfResponse> response) {
                    if (response.isSuccessful()) {
                        // Remove review from list and notify adapter
                        reviews.remove(positionToRemove);
                        notifyItemRemoved(positionToRemove);
                        notifyItemRangeChanged(positionToRemove, reviews.size());
                        Toast.makeText(context, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete review", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AddToShelfResponse> call, Throwable t) {
                    Toast.makeText(context, "Network error while deleting review", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        RatingBar ratingBar;
        TextView reviewText;
        LinearLayout actionButtons;
        Button editBtn;
        Button deleteBtn;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            reviewText = itemView.findViewById(R.id.reviewText);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
