package com.example.ctrlaltelite;

import android.widget.TextView;

import com.google.firebase.Timestamp;

/**
 * Class for comments on mood events
 */
public class CommentData {
    private String text;
    private String username;           // Unique ID or login name
    private String displayName;        // Full name or display name
    private Timestamp timestamp;

    /**
     * Empty constructor
     */
    public CommentData() {}

    /**
     * Constructor
     * @param text - text of the comment
     * @param displayName - display name of user sending comment
     * @param username - username of user sending comment
     * @param timestamp - timestamp of comment
     */
    public CommentData(String text, String displayName, String username, Timestamp timestamp) {
        this.text = text;
        this.username = username;
        this.displayName = displayName;
        this.timestamp = timestamp;
    }

    /**
     * Getter for text
     * @return the text of comment
     */
    public String getText() {
        return text;
    }

    /**
     * Setter for text
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter for username
     * @return username of the user sending the comment
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for username
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter for the display name
     * @return display name of the user
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for display name
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for timestamp
     * @return timestamp of message
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Setter for timestamp
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Formatted time stamp
     * @return a formatted version of the timestamp
     */
    public String getFormattedTimestamp() {
        if (timestamp == null) return "Unknown Date";
        return new java.text.SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", java.util.Locale.US)
                .format(timestamp.toDate());
    }
}
