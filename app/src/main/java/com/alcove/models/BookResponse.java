package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("average_rating")
    private float averageRating;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public float getAverageRating() { return averageRating; }
}

