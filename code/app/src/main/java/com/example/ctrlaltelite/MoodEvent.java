package com.example.ctrlaltelite;

import android.graphics.Picture;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Locale;
import java.text.SimpleDateFormat;


/**
 * Represents a mood event recorded by a user.
 * Stores details like emotional state, reason, trigger, social situation, timestamp, location, and an optional image.
 */
public class MoodEvent {

    private boolean isPublic;


    /** The emotional state of the user */
    private String emotionalState;
    /** The social situation during the mood event */
    private String socialSituation;
    /** The timestamp when the event was recorded */
    private Timestamp  timestamp;
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

    private String formattedTimestamp;

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
        this.timestamp = Timestamp.now(); // Set current timestamp
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
    public MoodEvent(String emotionalState, String reason, String socialSituation, Timestamp timestamp, GeoPoint location, String imgPath, String Username, boolean isPublic) {

        this.emotionalState = emotionalState;
        this.reason = reason;
        this.socialSituation = socialSituation;
        this.timestamp = timestamp;
        this.location = location;
        this.imgPath = imgPath;
        this.Username = Username;
        this.isPublic = isPublic;

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
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    /** @return The timestamp of the event. */
    public Timestamp getTimestamp() {
        return timestamp;
    }


    /**  Convert Timestamp to String for displaying in ListView. */
    public String getFormattedTimestamp() {
        if (timestamp == null) return "Unknown Date";
        return new java.text.SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", java.util.Locale.US)
                .format(timestamp.toDate());
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


    /** @return The value of isPublic */
    public boolean isPublic() {
        return isPublic;
    }
    /** set the value of isPublic */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

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
                ", socialSituation='" + socialSituation + '\'' +
                ", timestamp='" + getFormattedTimestamp() + '\'' +
                ", isPublic=" + isPublic +
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
