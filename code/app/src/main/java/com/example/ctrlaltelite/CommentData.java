package com.example.ctrlaltelite;


public class CommentData {
    private String text;
    private String username;
    private long timestamp;

    // No-argument constructor required by Firestore
    public CommentData() {
        // Firestore needs a no-argument constructor for deserialization
    }

    // Constructor with parameters (for creating new comments)
    public CommentData(String text, String username, long timestamp) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
