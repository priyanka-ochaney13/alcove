package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class UserPreferencesUpdateRequest {
    @SerializedName("monthly_reading_goal")
    private Integer monthlyReadingGoal;

    @SerializedName("reading_reminder_enabled")
    private Boolean readingReminderEnabled;

    @SerializedName("reading_reminder_time")
    private String readingReminderTime;

    @SerializedName("reading_reminder_days")
    private String readingReminderDays;

    @SerializedName("favorite_genres")
    private String favoriteGenres;

    // Constructors
    public UserPreferencesUpdateRequest() {}

    // Getters and Setters
    public Integer getMonthlyReadingGoal() { return monthlyReadingGoal; }
    public void setMonthlyReadingGoal(Integer monthlyReadingGoal) { this.monthlyReadingGoal = monthlyReadingGoal; }

    public Boolean getReadingReminderEnabled() { return readingReminderEnabled; }
    public void setReadingReminderEnabled(Boolean readingReminderEnabled) { this.readingReminderEnabled = readingReminderEnabled; }

    public String getReadingReminderTime() { return readingReminderTime; }
    public void setReadingReminderTime(String readingReminderTime) { this.readingReminderTime = readingReminderTime; }

    public String getReadingReminderDays() { return readingReminderDays; }
    public void setReadingReminderDays(String readingReminderDays) { this.readingReminderDays = readingReminderDays; }

    public String getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }
}
