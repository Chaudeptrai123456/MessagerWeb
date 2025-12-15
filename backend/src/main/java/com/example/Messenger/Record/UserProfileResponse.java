package com.example.Messenger.Record;

public class UserProfileResponse {
    private String id;
    private String email;
    private String avatar;

    public UserProfileResponse(String id, String email, String avatar) {
        this.id = id;
        this.email = email;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
