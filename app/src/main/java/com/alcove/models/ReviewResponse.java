package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class ReviewResponse {
    private int id;
    private String title;
    private String content;
    private Integer rating;
    private UserResponse user;
    private BookResponse book;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Integer getRating() { return rating; }
    public UserResponse getUser() { return user; }
    public BookResponse getBook() { return book; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    public static class UserResponse {
        private int id;
        private String username;
        public int getId() { return id; }
        public String getUsername() { return username; }
    }

    public static class BookResponse {
        private int id;
        private String title;
        private List<AuthorResponse> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private String isbn;
        private String language;
        private int pageCount;
        private String thumbnail;
        private String imageUrl;
        private String previewLink;
        private String infoLink;
        private String canonicalVolumeLink;

        public int getId() { return id; }
        public String getTitle() { return title; }
        public List<AuthorResponse> getAuthors() { return authors; }
        public String getPublisher() { return publisher; }
        public String getPublishedDate() { return publishedDate; }
        public String getDescription() { return description; }
        public String getIsbn() { return isbn; }
        public String getLanguage() { return language; }
        public int getPageCount() { return pageCount; }
        public String getThumbnail() { return thumbnail; }
        public String getImageUrl() { return imageUrl != null ? imageUrl : thumbnail; }
        public String getPreviewLink() { return previewLink; }
        public String getInfoLink() { return infoLink; }
        public String getCanonicalVolumeLink() { return canonicalVolumeLink; }
    }

    public static class AuthorResponse {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }
}
