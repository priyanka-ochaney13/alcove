package com.alcove;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.AddBookToShelfRequest;
import com.alcove.models.AddToShelfResponse;
import com.alcove.models.ShelfStatusResponse;
import com.alcove.models.CustomShelfResponse;
import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BookAdapter - RecyclerView adapter for displaying books
 * Demonstrates using Intents to navigate to detail screen with data
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private Context context;
    private List<Book> books;
    private boolean isBookshelfMode;

    public BookAdapter(Context context, List<Book> books) {
        this(context, books, false);
    }

    public BookAdapter(Context context, List<Book> books, boolean isBookshelfMode) {
        this.context = context;
        this.books = books;
        this.isBookshelfMode = isBookshelfMode;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);

        holder.bookTitle.setText(book.getTitle());
        holder.bookAuthor.setText(book.getAuthor());
        holder.bookRating.setRating(book.getRating());
        holder.ratingCount.setText(context.getString(R.string.ratings, book.getRatingCount()));

        if (holder.bookCover != null) {
            String imageUrl = book.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(holder.bookCover);
            } else {
                holder.bookCover.setImageResource(R.drawable.ic_book_placeholder);
            }
        }

        // Book click listener - demonstrates Intent creation and data passing
        holder.itemView.setOnClickListener(v -> navigateToBookDetails(book));

        // Long click for gesture handling - add to shelf
        holder.itemView.setOnLongClickListener(v -> {
            Toast.makeText(context, "Long press: Added to shelf " + book.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });

        // Add to shelf button click listener
        if (isBookshelfMode) {
            holder.addToShelfBtn.setVisibility(View.GONE);
        } else {
            holder.addToShelfBtn.setVisibility(View.VISIBLE);
            // Check shelf status
            checkShelfStatus(book, holder.addToShelfBtn);
            holder.addToShelfBtn.setOnClickListener(v -> {
                // Check current shelf status and handle accordingly
                checkShelfStatusAndHandle(book, holder.addToShelfBtn);
            });
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    /**
     * Navigate to BookDetailsActivity with book data
     * Demonstrates proper Intent usage with multiple data types
     */
    private void navigateToBookDetails(Book book) {
        Intent intent = new Intent(context, BookDetailsActivity.class);

        // Pass all book data via Intent extras
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, book.getId());
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_TITLE, book.getTitle());
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_AUTHOR, book.getAuthor());
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_RATING, book.getRating());
        intent.putExtra(BookDetailsActivity.EXTRA_RATING_COUNT, book.getRatingCount());
        intent.putExtra("EXTRA_IMAGE_URL", book.getImageUrl());

        context.startActivity(intent);
    }

    /**
     * Add book to shelf
     */
    private void addBookToShelf(int bookId, int shelfId) {
        ApiService apiService = ApiClient.getService();
        AddBookToShelfRequest request = new AddBookToShelfRequest(bookId);

        Call<AddToShelfResponse> call = apiService.addBookToShelf(shelfId, request);
        call.enqueue(new Callback<AddToShelfResponse>() {
            @Override
            public void onResponse(Call<AddToShelfResponse> call, Response<AddToShelfResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to add book to shelf", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddToShelfResponse> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Check the shelf status of a book and update the button accordingly
     */
    private void checkShelfStatus(Book book, Button addToShelfBtn) {
        ApiService apiService = ApiClient.getService();
        Call<ShelfStatusResponse> call = apiService.getBookShelfStatus(book.getId());
        call.enqueue(new Callback<ShelfStatusResponse>() {
            @Override
            public void onResponse(Call<ShelfStatusResponse> call, Response<ShelfStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String shelf = response.body().getShelf();
                    if (shelf != null && !shelf.isEmpty()) {
                        addToShelfBtn.setText(R.string.remove_from_shelf);
                    } else {
                        addToShelfBtn.setText(R.string.add_to_shelf);
                    }
                } else {
                    addToShelfBtn.setText(R.string.add_to_shelf);
                }
            }

            @Override
            public void onFailure(Call<ShelfStatusResponse> call, Throwable t) {
                // On failure, default to add to shelf
                addToShelfBtn.setText(R.string.add_to_shelf);
            }
        });
    }

    /**
     * Check shelf status and handle add/remove accordingly
     */
    private void checkShelfStatusAndHandle(Book book, Button addToShelfBtn) {
        ApiService apiService = ApiClient.getService();
        Call<ShelfStatusResponse> call = apiService.getBookShelfStatus(book.getId());
        call.enqueue(new Callback<ShelfStatusResponse>() {
            @Override
            public void onResponse(Call<ShelfStatusResponse> call, Response<ShelfStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String shelf = response.body().getShelf();
                    if (shelf != null && !shelf.isEmpty()) {
                        // Book is on a shelf, show remove confirmation
                        showRemoveConfirmationDialog(book, shelf);
                    } else {
                        // Book is not on a shelf, show shelf selection
                        showShelfSelectionDialog(book);
                    }
                } else {
                    // Default to showing shelf selection
                    showShelfSelectionDialog(book);
                }
            }

            @Override
            public void onFailure(Call<ShelfStatusResponse> call, Throwable t) {
                // On failure, default to showing shelf selection
                showShelfSelectionDialog(book);
            }
        });
    }

    /**
     * Show shelf selection dialog
     */
    private void showShelfSelectionDialog(Book book) {
        ApiService apiService = ApiClient.getService();
        Call<List<CustomShelfResponse>> call = apiService.getUserShelves();

        call.enqueue(new Callback<List<CustomShelfResponse>>() {
            @Override
            public void onResponse(Call<List<CustomShelfResponse>> call, Response<List<CustomShelfResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CustomShelfResponse> shelves = response.body();
                    if (shelves.isEmpty()) {
                        Toast.makeText(context, "No shelves available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create array of shelf names
                    String[] shelfNames = new String[shelves.size()];
                    for (int i = 0; i < shelves.size(); i++) {
                        shelfNames[i] = shelves.get(i).getName();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Add to Shelf")
                           .setItems(shelfNames, (dialog, which) -> {
                               CustomShelfResponse selectedShelf = shelves.get(which);
                               addBookToShelf(book.getId(), selectedShelf.getId());
                           })
                           .setNegativeButton("Cancel", null)
                           .show();
                } else {
                    Toast.makeText(context, "Failed to load shelves", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CustomShelfResponse>> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show remove confirmation dialog
     */
    private void showRemoveConfirmationDialog(Book book, String currentShelf) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove from Shelf")
               .setMessage("Remove \"" + book.getTitle() + "\" from " + currentShelf + "?")
               .setPositiveButton("Remove", (dialog, which) -> {
                   // Implement remove functionality
                   removeBookFromShelf(book.getId());
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    /**
     * Remove book from shelf
     */
    private void removeBookFromShelf(int bookId) {
        ApiService apiService = ApiClient.getService();
        Call<AddToShelfResponse> call = apiService.removeBookFromShelf(bookId);
        call.enqueue(new Callback<AddToShelfResponse>() {
            @Override
            public void onResponse(Call<AddToShelfResponse> call, Response<AddToShelfResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Refresh shelf status to update button
                    // Note: This assumes the adapter is used in a context where we can refresh
                    // You may need to notify the adapter or refresh the activity/fragment
                } else {
                    Toast.makeText(context, "Failed to remove book from shelf", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddToShelfResponse> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        TextView bookAuthor;
        RatingBar bookRating;
        TextView ratingCount;
        ImageView bookCover;
        Button addToShelfBtn;
        private Book boundBook;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
            bookRating = itemView.findViewById(R.id.bookRating);
            ratingCount = itemView.findViewById(R.id.ratingCount);
            bookCover = itemView.findViewById(R.id.bookCover);
            addToShelfBtn = itemView.findViewById(R.id.addToShelfBtn);

            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (boundBook != null) {
                        Toast.makeText(context, "Double tap: Favorite " + boundBook.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false; // Allow other listeners like click to work
            });
        }

        public void bind(Book book) {
            this.boundBook = book;
        }
    }
}
