package com.alcove;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
 * Fragment to display books for each bookshelf tab
 * Demonstrates Fragment usage with arguments
 */
public class BookshelfFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "tab_type";
    public static final int TAB_CURRENTLY_READING = 0;
    public static final int TAB_WANT_TO_READ = 1;
    public static final int TAB_READ = 2;

    private int tabType;
    private RecyclerView booksRecycler;
    private BookAdapter bookAdapter;
    private List<Book> books = new ArrayList<>();

    /**
     * Factory method to create fragment with arguments
     * This demonstrates proper Fragment instantiation with data passing
     */
    public static BookshelfFragment newInstance(int tabType) {
        BookshelfFragment fragment = new BookshelfFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getInt(ARG_TAB_TYPE, TAB_CURRENTLY_READING);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookshelf, container, false);

        booksRecycler = view.findViewById(R.id.booksRecycler);
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        if (getContext() == null) {
            return;
        }
        bookAdapter = new BookAdapter(getContext(), books, true);
        booksRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        booksRecycler.setAdapter(bookAdapter);
        fetchBooks();
    }

    /**
     * Get books based on the tab type
     */
    private void fetchBooks() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<BookResponse>> call = null;

        switch (tabType) {
            case TAB_CURRENTLY_READING:
                call = apiService.getCurrentlyReading();
                break;
            case TAB_WANT_TO_READ:
                call = apiService.getWantToRead();
                break;
            case TAB_READ:
                call = apiService.getAlreadyRead();
                break;
        }

        if (call != null) {
            call.enqueue(new Callback<List<BookResponse>>() {
                @Override
                public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                    try {
                        if (!isAdded() || getView() == null || bookAdapter == null) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            books.clear();
                            for (BookResponse b : response.body()) {
                                books.add(new Book(b.getId(), b.getTitle(), "", b.getAverageRating(), 0, b.getImageUrl()));
                            }
                            if (bookAdapter != null) {
                                bookAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("BookshelfFragment", "Error in onResponse", e);
                    }
                }

                @Override
                public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                    Log.e("BookshelfFragment", "Failed to fetch shelf items", t);
                }
            });
        }
    }

    /**
     * Public method to refresh data (can be called from Activity)
     */
    public void refreshBooks() {
        if (bookAdapter != null) {
            setupRecyclerView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh books when fragment becomes visible
        if (bookAdapter != null) {
            fetchBooks();
        }
    }
}
