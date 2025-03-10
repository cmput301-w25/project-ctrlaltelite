package com.example.ctrlaltelite;

import android.graphics.Picture;

import com.google.firebase.firestore.GeoPoint;

/**
 * Represents a mood event recorded by a user.
 * Stores details like emotional state, reason, trigger, social situation, timestamp, location, and an optional image.
 */
public class MoodEvent {

    /** The emotional state of the user */
    private String emotionalState;
    /** The trigger causing the mood */
    private String trigger;
    /** The social situation during the mood event */
    private String socialSituation;
    /** The timestamp when the event was recorded */
    private String timestamp;
    /** The username of the user who recorded the event */
    private String Username;
    /** The reason for the mood */
    private String reason;
    /** Path to the uploaded image (if any) */
    private String imgPath;
    /** Unique document ID in Firestore */
    private String documentId;
    /** The geographical location where the mood event was recorded */
    private GeoPoint location;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public MoodEvent() {
        // Empty constructor
    }



    /**
     * Constructor to initialize a mood event with only an emotional state.
     * @param emotionalState
     */
    public MoodEvent(String emotionalState) {
        this.emotionalState = emotionalState;
        this.timestamp = getTimestamp(); // Set current timestamp
    }

    /**
     * Constructor to initialize all fields.
     * @param emotionalState The emotional state of the user.
     * @param reason The reason for the mood event.
     * @param trigger The trigger for the mood event.
     * @param socialSituation The social situation during the event.
     * @param timestamp The timestamp of when the event occurred.
     * @param location The geographical location of the event.
     * @param imgPath The path to the uploaded image.
     * @param Username The username of the person recording the event.
     */
    public MoodEvent(String emotionalState, String reason, String trigger, String socialSituation, String timestamp, GeoPoint location, String imgPath, String Username) {

        this.emotionalState = emotionalState;
        this.reason = reason;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.timestamp = timestamp;
        this.location = location;
        this.imgPath = imgPath;
        this.Username = Username;

    }

    /** @return The reason for the mood. */
    public String getReason() {
        return reason;
    }
    /** Sets the reason for the mood. */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /** sets the timestamp of the event. */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    /** @return The timestamp of the event. */
    public String getTimestamp() {
        return timestamp;
    }

    /** @return The location of the event. */
    public GeoPoint getLocation() {
        return location;
    }
    /** set the location of the event. */
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    /** @return The emotional state of the user. */
    public String getEmotionalState() {
        return emotionalState;
    }
    /** set the emotional state of the user. */
    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }

    /** @return The trigger causing the mood. */
    public String getTrigger() {
        return trigger;
    }
    /** set the trigger causing the mood. */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /** @return The social situation at the time of the event. */
    public String getSocialSituation() {
        return socialSituation;
    }
    /** set the social situation at the time of the event. */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /** @return The Firestore document ID. */
    public String getDocumentId() { return documentId; }
    /** set the Firestore document ID. */
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    /** @return The username of the user who recorded the event. */
    public String getUsername() { return Username;}
    /** set the username of the user who recorded the event. */
    public void setUsername(String username) { this.Username = username;}


    /**
     * Provides a string representation of the MoodEvent object.
     * @return A formatted string with event details.
     */
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

    /** @return The image path associated with the mood event. */
    public String getImgPath() {
        return imgPath;
    }
    /** set the image path associated with the mood event. */
    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
