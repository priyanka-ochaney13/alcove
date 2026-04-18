package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class FirebaseSyncResponse {
    @SerializedName("user")
    private UserResponse user;

    @SerializedName("is_new_user")
    private boolean isNewUser;

    public UserResponse getUser() { return user; }
    public boolean isNewUser() { return isNewUser; }
}
