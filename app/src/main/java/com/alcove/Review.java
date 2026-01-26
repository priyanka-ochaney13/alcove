package com.alcove;

public class Review {
    private int id;
    private String userName;
    private int rating;
    private String reviewText;

    public Review(int id, String userName, int rating, String reviewText) {
        this.id = id;
        this.userName = userName;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }
}
