package com.alcove;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.BookResponse;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment to display a horizontal list of books
 * Used in MainActivity for Popular, Trending, and Recommended sections
 */
public class BooksListFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    public static final String CATEGORY_POPULAR = "popular";
    public static final String CATEGORY_TRENDING = "trending";
    public static final String CATEGORY_RECOMMENDED = "recommended";

    private String category;
    private RecyclerView booksRecycler;
    private BookAdapter bookAdapter;
    private List<Book> books = new ArrayList<>();
    private Call<List<BookResponse>> currentCall;

    /**
     * Factory method to create fragment with category argument
     */
    public static BooksListFragment newInstance(String category) {
        BooksListFragment fragment = new BooksListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY, CATEGORY_POPULAR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books_list, container, false);

        booksRecycler = view.findViewById(R.id.booksRecycler);
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        if (getContext() == null) {
            return;
        }
        bookAdapter = new BookAdapter(getContext(), books);

        // Horizontal layout for the book list
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        booksRecycler.setLayoutManager(layoutManager);

        // When a horizontal RecyclerView is placed inside a vertical ScrollView,
        // disable nested scrolling and avoid claiming fixed size so it measures height correctly.
        booksRecycler.setNestedScrollingEnabled(false);
        booksRecycler.setHasFixedSize(false);
        booksRecycler.setAdapter(bookAdapter);

        // Ensure the RecyclerView has a stable height (from dimens) so neighboring sections don't overlap.
        int height = (int) getResources().getDimension(R.dimen.book_list_height);
        ViewGroup.LayoutParams lp = booksRecycler.getLayoutParams();
        lp.height = height;
        booksRecycler.setLayoutParams(lp);

        // Delay API call to avoid startup ANR
        booksRecycler.post(this::fetchBooks);
    }

    /**
     * Get books based on category
     */
    private void fetchBooks() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<BookResponse>> call;

        switch (category) {
            case CATEGORY_POPULAR:
                call = apiService.getPopularBooks(10);
                break;
            case CATEGORY_TRENDING:
                call = apiService.getTrendingBooks(10);
                break;
            case CATEGORY_RECOMMENDED:
                call = apiService.getRecommendedBooks(10);
                break;
            default:
                call = apiService.getPopularBooks(10);
                break;
        }

        if (call != null) {
            currentCall = call;
            call.enqueue(new Callback<List<BookResponse>>() {
                @Override
                public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                    if (!isAdded() || getView() == null || bookAdapter == null) {
                        return;
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        books.clear();
                        for (BookResponse b : response.body()) {
                            // Extract author name from authors list
                            String authorName = "";
                            if (b.getAuthors() != null && !b.getAuthors().isEmpty()) {
                                authorName = b.getAuthors().get(0).getName();
                            }
                            books.add(new Book(b.getId(), b.getTitle(), authorName, b.getAverageRating(), b.getRatingsCount(), b.getImageUrl()));
                        }
                        if (bookAdapter != null) {
                            bookAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                    if (!isAdded()) {
                        return;
                    }
                    Log.e("BooksListFragment", "Failed to get books: " + t.getMessage());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        booksRecycler = null;
        bookAdapter = null;
    }
}
