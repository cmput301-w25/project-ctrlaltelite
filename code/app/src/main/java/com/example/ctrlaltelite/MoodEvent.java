package com.example.ctrlaltelite;

import com.google.firebase.firestore.GeoPoint;

public class MoodEvent {

    private String emotionalState;  // Required
    private String trigger;         // Optional
    private String socialSituation; // Optional
    private String timestamp;       // Required
    private String documentId;
    private String Username;
    private String reason;

    //private Picture picture;

    private GeoPoint location;

    public MoodEvent() {
        // Empty constructor
    }

    // Constructor to initialize the required fields (emotional state and timestamp)
    public MoodEvent(String emotionalState) {
        this.emotionalState = emotionalState;
        this.timestamp = getTimestamp(); // Set current timestamp
    }

    // Constructor to initialize all fields (emotional state, trigger, and social situation)
    public MoodEvent(String emotionalState, String reason, String trigger, String socialSituation, String timestamp, GeoPoint location, String Username) {
        this.emotionalState = emotionalState;
        this.reason = reason;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.timestamp = timestamp;
        this.location = location;
        this.Username = Username;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Getter for Timestamp
    public String getTimestamp() {
        return timestamp;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    // Getter and Setter for Emotional State
    public String getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    // Getter and Setter for Trigger
    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    // Getter and Setter for Social Situation
    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }


    public String getUsername() { return Username;}

    public void setUsername(String username) { this.Username = username;}

    // toString method to display the mood event
    @Override
    public String toString() {
        return "MoodEvent{" +
                "Username='" + Username + '\'' +
                "DocID='" + documentId + '\'' +
                "emotionalState='" + emotionalState + '\'' +
                ", trigger='" + trigger + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
