package com.alcove.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String imageUrl;

    public Book() {}

    public Book(int id, String title, String author, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
