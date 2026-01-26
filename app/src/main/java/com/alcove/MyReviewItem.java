package com.alcove;

public class MyReviewItem {
    private int id;
    private String bookTitle;
    private String bookAuthor;
    private int rating;
    private String reviewText;

    public MyReviewItem(int id, String bookTitle, String bookAuthor, int rating, String reviewText) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
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

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }
}
