package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ReadingProgressResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("book_id")
    private int bookId;

    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("total_pages")
    private Integer totalPages;

    @SerializedName("start_date")
    private Date startDate;

    @SerializedName("end_date")
    private Date endDate;

    @SerializedName("is_completed")
    private boolean isCompleted;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getBookId() { return bookId; }
    public int getCurrentPage() { return currentPage; }
    public Integer getTotalPages() { return totalPages; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public boolean isCompleted() { return isCompleted; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    // Setters
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
