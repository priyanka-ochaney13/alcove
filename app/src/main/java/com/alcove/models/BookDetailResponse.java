package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookDetailResponse extends BookResponse {
    @SerializedName("ai_review_summary")
    private String aiReviewSummary;

    @SerializedName("reviews")
    private List<ReviewResponse> reviews;

    public String getAiReviewSummary() { return aiReviewSummary; }
    public List<ReviewResponse> getReviews() { return reviews; }
}

