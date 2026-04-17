package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class UserUpdateRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("bio")
    private String bio;

    @SerializedName("avatar_url")
    private String avatarUrl;

    public UserUpdateRequest() {}

    // Constructor for partial updates
    public UserUpdateRequest(String email, String username, String fullName, String bio, String avatarUrl) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
