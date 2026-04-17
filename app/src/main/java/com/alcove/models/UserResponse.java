package com.alcove.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class UserResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("firebase_uid")
    private String firebaseUid;

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

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("is_superuser")
    private boolean isSuperuser;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    // Getters
    public int getId() { return id; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isActive() { return isActive; }
    public boolean isSuperuser() { return isSuperuser; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
}
