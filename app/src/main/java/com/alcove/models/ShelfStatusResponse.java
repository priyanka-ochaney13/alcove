package com.alcove.models;

public class ShelfStatusResponse {
    private String shelf;
    private Integer shelfId;

    public ShelfStatusResponse() {}

    public ShelfStatusResponse(String shelf, Integer shelfId) {
        this.shelf = shelf;
        this.shelfId = shelfId;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public Integer getShelfId() {
        return shelfId;
    }

    public void setShelfId(Integer shelfId) {
        this.shelfId = shelfId;
    }
}
