package com.alcove;

public class Review {
    private int id;
    private String userName;
    private String userId;
    private int rating;
    private String reviewText;

    public Review(int id, String userName, String userId, int rating, String reviewText) {
        this.id = id;
        this.userName = userName;
        this.userId = userId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
