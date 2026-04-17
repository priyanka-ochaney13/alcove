package com.alcove;

public class Book {
    private int id;
    private String title;
    private String author;
    private float rating;
    private int ratingCount;
    private String imageUrl;

    public Book(int id, String title, String author, float rating, int ratingCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }

    public Book(int id, String title, String author, float rating, int ratingCount, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public float getRating() {
        return rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
