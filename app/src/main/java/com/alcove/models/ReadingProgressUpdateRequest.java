package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ReadingProgressUpdateRequest {
    @SerializedName("current_page")
    private Integer currentPage;

    @SerializedName("total_pages")
    private Integer totalPages;

    @SerializedName("start_date")
    private Date startDate;

    @SerializedName("end_date")
    private Date endDate;

    @SerializedName("is_completed")
    private Boolean isCompleted;

    // Constructors
    public ReadingProgressUpdateRequest() {}

    public ReadingProgressUpdateRequest(Integer currentPage, Integer totalPages, Date startDate, Date endDate, Boolean isCompleted) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
}
