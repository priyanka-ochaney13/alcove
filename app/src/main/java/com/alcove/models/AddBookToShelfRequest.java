package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class AddBookToShelfRequest {
    @SerializedName("book_id")
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
