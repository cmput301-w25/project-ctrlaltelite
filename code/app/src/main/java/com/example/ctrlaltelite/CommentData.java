package com.example.ctrlaltelite;

import com.google.firebase.Timestamp;

public class CommentData {
    private String text;
    private String username;
    private Timestamp timestamp; // Use Firebase Timestamp

    // No-argument constructor required by Firestore
    public CommentData() {
        // Firestore needs a no-argument constructor for deserialization
    }

    // Constructor with parameters (for creating new comments)
    public CommentData(String text, String username, Timestamp timestamp) {
        this.text = text;
        this.username = username;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
