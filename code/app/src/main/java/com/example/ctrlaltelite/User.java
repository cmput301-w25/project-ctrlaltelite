package com.example.ctrlaltelite;

public class User {
    private String displayName;
    private String username;
    private String email;
    private String mobile;
    private String profilePhotoUrl; // Optional, null if not present
    public User() {}
    public User(String displayName, String username, String profilePhotoUrl) {
        this.displayName = displayName;
        this.username = username;
        this.profilePhotoUrl = profilePhotoUrl;
    }
    // Name currently not being asking during sign up
    public String getDisplayName() { return displayName;}

    public void setDisplayName(String displayName) { this.displayName = displayName;}

    public String getUsername() { return username;}

    public void setUsername(String username) { this.username = username;}

    public String getEmail() { return email;}

    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile;}

    public void setMobile(String mobile) { this.mobile = mobile;}

    // To do: Profile photo implementation later (takes too long)
    public String getProfilePhotoUrl() { return profilePhotoUrl;}

    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
}
