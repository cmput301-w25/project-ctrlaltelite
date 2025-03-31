package com.example.ctrlaltelite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents a user with basic profile information like display name, username, etc.
 */
public class User implements Serializable {
    private String displayName;
    private String username;
    private String email;
    private String mobile;
    private String profilePhotoUrl; // Optional, null if not present

    /** Default constructor for creating an empty User object. */
    public User() {}

    /**
     * Constructor for creating a User with essential profile information.
     *
     * @param displayName     The user's display name
     * @param username        The user's unique username
     * @param profilePhotoUrl URL to the user's profile photo, may be null
     */
    public User(String displayName, String username, String profilePhotoUrl) {
        this.displayName = displayName;
        this.username = username;
        this.profilePhotoUrl = profilePhotoUrl;
    }
    // Name currently not being asking during sign up
    /**  @return The display name */
    public String getDisplayName() { return displayName;}

    /** Set the display name */
    public void setDisplayName(String displayName) { this.displayName = displayName;}

    /**  @return The username */
    public String getUsername() { return username;}

    /** Set the username */
    public void setUsername(String username) { this.username = username;}

    /**  @return The email */
    public String getEmail() { return email;}

    /** Set the email */
    public void setEmail(String email) { this.email = email; }

    /**  @return The mobile */
    public String getMobile() { return mobile;}

    /** Set the mobile number */
    public void setMobile(String mobile) { this.mobile = mobile;}

    // To do: Profile photo implementation later (takes too long)
    /** @return the profile photo */
    public String getProfilePhotoUrl() { return profilePhotoUrl;}

    /** Set the profile photo */
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
}
