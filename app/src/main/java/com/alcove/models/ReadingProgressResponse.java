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
    private Boolean isCompleted;

    @SerializedName("shelf_movement_success")
    private Boolean shelfMovementSuccess;

    @SerializedName("shelf_movement_message")
    private String shelfMovementMessage;

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getBookId() { return bookId; }
    public int getCurrentPage() { return currentPage; }
    public Integer getTotalPages() { return totalPages; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public Boolean getIsCompleted() { return isCompleted; }
    public Boolean isCompleted() { return isCompleted != null && isCompleted; }

    public Boolean getShelfMovementSuccess() { return shelfMovementSuccess; }
    public String getShelfMovementMessage() { return shelfMovementMessage; }

    // Setters
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public void setShelfMovementSuccess(Boolean shelfMovementSuccess) { this.shelfMovementSuccess = shelfMovementSuccess; }
    public void setShelfMovementMessage(String shelfMovementMessage) { this.shelfMovementMessage = shelfMovementMessage; }
}
