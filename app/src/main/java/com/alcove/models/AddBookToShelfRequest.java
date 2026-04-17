package com.alcove.models;

public class AddBookToShelfRequest {
    private int book_id;

    public AddBookToShelfRequest() {}

    public AddBookToShelfRequest(int book_id) {
        this.book_id = book_id;
    }

    public int getBookId() {
        return book_id;
    }

    public void setBookId(int book_id) {
        this.book_id = book_id;
    }
}
