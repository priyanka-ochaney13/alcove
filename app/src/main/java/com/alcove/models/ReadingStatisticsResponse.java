package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class ReadingStatisticsResponse {
    @SerializedName("books_read_this_year")
    private int booksReadThisYear;

    @SerializedName("monthly_reading_goal")
    private int monthlyReadingGoal;

    @SerializedName("current_month_progress")
    private int currentMonthProgress;

    @SerializedName("total_books_read")
    private int totalBooksRead;

    @SerializedName("currently_reading")
    private int currentlyReading;

    @SerializedName("average_rating_given")
    private Float averageRatingGiven;

    @SerializedName("favorite_genre")
    private String favoriteGenre;

    // Getters
    public int getBooksReadThisYear() { return booksReadThisYear; }
    public int getMonthlyReadingGoal() { return monthlyReadingGoal; }
    public int getCurrentMonthProgress() { return currentMonthProgress; }
    public int getTotalBooksRead() { return totalBooksRead; }
    public int getCurrentlyReading() { return currentlyReading; }
    public Float getAverageRatingGiven() { return averageRatingGiven; }
    public String getFavoriteGenre() { return favoriteGenre; }

    // Setters
    public void setBooksReadThisYear(int booksReadThisYear) { this.booksReadThisYear = booksReadThisYear; }
    public void setMonthlyReadingGoal(int monthlyReadingGoal) { this.monthlyReadingGoal = monthlyReadingGoal; }
    public void setCurrentMonthProgress(int currentMonthProgress) { this.currentMonthProgress = currentMonthProgress; }
    public void setTotalBooksRead(int totalBooksRead) { this.totalBooksRead = totalBooksRead; }
    public void setCurrentlyReading(int currentlyReading) { this.currentlyReading = currentlyReading; }
    public void setAverageRatingGiven(Float averageRatingGiven) { this.averageRatingGiven = averageRatingGiven; }
    public void setFavoriteGenre(String favoriteGenre) { this.favoriteGenre = favoriteGenre; }
}
