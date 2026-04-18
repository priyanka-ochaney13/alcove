package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class ReadingStatisticsResponse {
    @SerializedName("books_read_this_year")
    private int booksReadThisYear;

    @SerializedName("monthly_reading_goal")
    private int monthlyReadingGoal;

    @SerializedName("yearly_reading_goal")
    private int yearlyReadingGoal;

    @SerializedName("total_books_read")
    private int totalBooksRead;

    @SerializedName("currently_reading")
    private int currentlyReading;

    @SerializedName("want_to_read")
    private int wantToRead;

    @SerializedName("average_rating_given")
    private Float averageRatingGiven;

    public int getBooksReadThisYear() { return booksReadThisYear; }
    public int getMonthlyReadingGoal() { return monthlyReadingGoal; }
    public int getYearlyReadingGoal() { return yearlyReadingGoal; }
    public int getTotalBooksRead() { return totalBooksRead; }
    public int getCurrentlyReading() { return currentlyReading; }
    public int getWantToRead() { return wantToRead; }
    public Float getAverageRatingGiven() { return averageRatingGiven; }
}
