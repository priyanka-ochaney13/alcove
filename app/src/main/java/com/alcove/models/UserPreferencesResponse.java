package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class UserPreferencesResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("monthly_reading_goal")
    private int monthlyReadingGoal;

    @SerializedName("reading_reminder_enabled")
    private boolean readingReminderEnabled;

    @SerializedName("reading_reminder_time")
    private String readingReminderTime;

    @SerializedName("reading_reminder_days")
    private String readingReminderDays;

    @SerializedName("favorite_genres")
    private String favoriteGenres;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("prefer_audiobooks")
    private boolean preferAudiobooks;

    @SerializedName("yearly_reading_goal")
    private int yearlyReadingGoal;

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getMonthlyReadingGoal() { return monthlyReadingGoal; }
    public boolean isReadingReminderEnabled() { return readingReminderEnabled; }
    public String getReadingReminderTime() { return readingReminderTime; }
    public String getReadingReminderDays() { return readingReminderDays; }
    public String getFavoriteGenres() { return favoriteGenres; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public boolean prefersAudiobooks() { return preferAudiobooks; }
    public int getYearlyReadingGoal() { return yearlyReadingGoal; }

    // Setters
    public void setMonthlyReadingGoal(int monthlyReadingGoal) { this.monthlyReadingGoal = monthlyReadingGoal; }
    public void setReadingReminderEnabled(boolean readingReminderEnabled) { this.readingReminderEnabled = readingReminderEnabled; }
    public void setReadingReminderTime(String readingReminderTime) { this.readingReminderTime = readingReminderTime; }
    public void setReadingReminderDays(String readingReminderDays) { this.readingReminderDays = readingReminderDays; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }
}
