package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("average_rating")
    private float averageRating;

    @SerializedName("ratings_count")
    private int ratingsCount;

    @SerializedName("authors")
    private List<AuthorResponse> authors;

    @SerializedName("genres")
    private List<GenreResponse> genres;

    @SerializedName("google_id")
    private String googleId;

    @SerializedName("isbn_10")
    private String isbn10;

    @SerializedName("page_count")
    private int pageCount;

    @SerializedName("language")
    private String language;

    @SerializedName("isbn_13")
    private String isbn13;

    @SerializedName("published_date")
    private String publishedDate;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public float getAverageRating() { return averageRating; }
    public int getRatingsCount() { return ratingsCount; }
    public List<AuthorResponse> getAuthors() { return authors; }
    public List<GenreResponse> getGenres() { return genres; }
    public String getGoogleId() { return googleId; }
    public String getIsbn10() { return isbn10; }
    public int getPageCount() { return pageCount; }
    public String getLanguage() { return language; }
    public String getIsbn13() { return isbn13; }
    public String getPublishedDate() { return publishedDate; }

    public static class AuthorResponse {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("bio")
        private String bio;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getBio() { return bio; }
    }

    public static class GenreResponse {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }
}
