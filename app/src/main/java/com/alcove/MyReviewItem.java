package com.alcove;

public class MyReviewItem {
    private int id;
    private String bookTitle;
    private String bookAuthor;
    private float rating;
    private String reviewText;
    private String bookCoverUrl;

    public MyReviewItem(int id, String bookTitle, String bookAuthor, float rating, String reviewText) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public MyReviewItem(String bookTitle, String bookCoverUrl, float rating, String reviewText, String timeAgo) {
        this.bookTitle = bookTitle;
        this.bookCoverUrl = bookCoverUrl;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() {
        return id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public float getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }
}
