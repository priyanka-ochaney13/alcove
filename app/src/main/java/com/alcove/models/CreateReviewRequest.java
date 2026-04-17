package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class CreateReviewRequest {
    @SerializedName("book_id")
    private int bookId;

    private String title;
    private String content;
    private int rating;

    public CreateReviewRequest() {}

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
