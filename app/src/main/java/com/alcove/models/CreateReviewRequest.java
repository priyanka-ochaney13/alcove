package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class CreateReviewRequest {
    private String title;
    private String content;
    private int rating;

    public CreateReviewRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
