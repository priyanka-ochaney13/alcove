package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class UserPreferencesUpdateRequest {
    @SerializedName("monthly_reading_goal")
    private Integer monthlyReadingGoal;

    @SerializedName("yearly_reading_goal")
    private Integer yearlyReadingGoal;

    @SerializedName("favorite_genres")
    private String favoriteGenres;

    @SerializedName("timezone")
    private String timezone;

    // Constructors
    public UserPreferencesUpdateRequest() {}

    // Getters and Setters
    public Integer getMonthlyReadingGoal() { return monthlyReadingGoal; }
    public void setMonthlyReadingGoal(Integer monthlyReadingGoal) { this.monthlyReadingGoal = monthlyReadingGoal; }

    public Integer getYearlyReadingGoal() { return yearlyReadingGoal; }
    public void setYearlyReadingGoal(Integer yearlyReadingGoal) { this.yearlyReadingGoal = yearlyReadingGoal; }

    public String getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
