package com.alcove.models;

import com.google.gson.annotations.SerializedName;

public class FirebaseSyncRequest {
    @SerializedName("token")
    private String token;

    public FirebaseSyncRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
