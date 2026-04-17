package com.alcove.models;

public class ShelfStatusResponse {
    private String shelf;

    public ShelfStatusResponse() {}

    public ShelfStatusResponse(String shelf) {
        this.shelf = shelf;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }
}
