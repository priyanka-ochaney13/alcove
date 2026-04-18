package com.alcove.api;

import com.alcove.models.BookResponse;
import com.alcove.models.BookDetailResponse;
import com.alcove.models.ShelfStatusResponse;
import com.alcove.models.AddToShelfResponse;
import com.alcove.models.AddBookToShelfRequest;
import com.alcove.models.CustomShelfResponse;
import com.alcove.models.UserResponse;
import com.alcove.models.UserUpdateRequest;
import com.alcove.models.ReadingProgressResponse;
import com.alcove.models.ReadingProgressUpdateRequest;
import com.alcove.models.UserPreferencesResponse;
import com.alcove.models.UserPreferencesUpdateRequest;
import com.alcove.models.ReadingStatisticsResponse;
import com.alcove.models.CreateReviewRequest;
import com.alcove.models.ReviewResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;

public interface ApiService {
    @POST("users/auth")
    Call<com.alcove.models.FirebaseSyncResponse> syncUser(@Body com.alcove.models.FirebaseSyncRequest request);

    @GET("books")
    Call<List<BookResponse>> getBooks(
            @Query("skip") int skip,
            @Query("limit") int limit,
            @Query("genre_slug") String genreSlug
    );

    @GET("books/{book_id}")
    Call<BookDetailResponse> getBookDetails(@Path("book_id") int bookId);

    @GET("books/popular")
    Call<List<BookResponse>> getPopularBooks(@Query("limit") int limit);

    @GET("books/trending")
    Call<List<BookResponse>> getTrendingBooks(@Query("limit") int limit);

    @GET("books/recommended")
    Call<List<BookResponse>> getRecommendedBooks(@Query("limit") int limit);

    @GET("books/search")
    Call<List<BookResponse>> searchBooks(
            @Query("q") String query,
            @Query("author") String author,
            @Query("genre") String genre,
            @Query("min_rating") Float minRating,
            @Query("max_rating") Float maxRating,
            @Query("publication_year") Integer publicationYear,
            @Query("isbn") String isbn,
            @Query("limit") int limit
    );

    @GET("user/reviews")
    Call<List<ReviewResponse>> getUserReviews();

    @POST("books/{book_id}/reviews")
    Call<ReviewResponse> createReview(@Path("book_id") int bookId, @Body CreateReviewRequest request);

    @PUT("books/{book_id}/reviews")
    Call<ReviewResponse> updateReview(@Path("book_id") int bookId, @Body CreateReviewRequest request);

    @GET("user/shelf/reading")
    Call<List<BookResponse>> getCurrentlyReading();

    @GET("user/shelf/want_to_read")
    Call<List<BookResponse>> getWantToRead();

    @GET("user/shelf/read")
    Call<List<BookResponse>> getAlreadyRead();

    @GET("user/shelves")
    Call<List<CustomShelfResponse>> getUserShelves();

    @GET("user/books/{book_id}/shelf-status")
    Call<ShelfStatusResponse> getBookShelfStatus(@Path("book_id") int bookId);

    @POST("shelves/{shelf_id}/books")
    Call<AddToShelfResponse> addBookToShelf(@Path("shelf_id") int shelfId, @Body AddBookToShelfRequest request);

    @DELETE("books/{book_id}/reviews")
    Call<AddToShelfResponse> deleteReview(@Path("book_id") int bookId);

    @DELETE("shelves/{shelf_id}/books/{book_id}")
    Call<AddToShelfResponse> removeBookFromShelf(@Path("shelf_id") int shelfId, @Path("book_id") int bookId);

    @GET("user/profile")
    Call<UserResponse> getUserProfile();

    @PUT("user/profile")
    Call<UserResponse> updateUserProfile(@Body UserUpdateRequest request);

    // Reading Progress endpoints
    @GET("user/reading-progress")
    Call<List<ReadingProgressResponse>> getUserReadingProgress();

    @GET("user/books/{book_id}/reading-progress")
    Call<ReadingProgressResponse> getBookReadingProgress(@Path("book_id") int bookId);

    @POST("user/books/{book_id}/reading-progress")
    Call<ReadingProgressResponse> updateReadingProgress(@Path("book_id") int bookId, @Body ReadingProgressUpdateRequest request);

    // User Preferences endpoints
    @GET("user/preferences")
    Call<UserPreferencesResponse> getUserPreferences();

    @PUT("user/preferences")
    Call<UserPreferencesResponse> updateUserPreferences(@Body UserPreferencesUpdateRequest request);

    // Statistics endpoint
    @GET("user/statistics")
    Call<ReadingStatisticsResponse> getUserStatistics();

    @GET("books/{book_id}/reading-progress")
    Call<ReadingProgressResponse> getBookReadingProgressDirect(@Path("book_id") int bookId);

    @GET("books/{book_id}/reviews")
    Call<List<ReviewResponse>> getBookReviews(@Path("book_id") int bookId, @Query("skip") int skip, @Query("limit") int limit);

    @POST("books/submit")
    Call<BookResponse> submitBook(@Body com.alcove.models.BookCreate request);
}
