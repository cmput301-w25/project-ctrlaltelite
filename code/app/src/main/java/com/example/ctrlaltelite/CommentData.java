package com.example.ctrlaltelite;

import android.widget.TextView;

import com.google.firebase.Timestamp;

public class CommentData {
    private String text;
    private String username;           // Unique ID or login name
    private String displayName;        // Full name or display name
    private Timestamp timestamp;

    // Required no-arg constructor
    public CommentData() {}

    // Updated constructor with both names
    public CommentData(String text, String displayName, String username, Timestamp timestamp) {
        this.text = text;
        this.username = username;
        this.displayName = displayName;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "Unknown Date";
        return new java.text.SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", java.util.Locale.US)
                .format(timestamp.toDate());
    }
}
