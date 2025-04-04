package com.example.ctrlaltelite;

import com.google.firebase.Timestamp;

/**
 * Building the model for chat messaging
 */
public class ChatMessageModel {
    private String message;
    private String senderId;
    private Timestamp timestamp;

    private boolean isRead;

    /**
     * Empty constructor
     */
    public ChatMessageModel() {
    }

    /**
     * Constructor for Chat Message Model
     * @param message - the message user wants to send
     * @param senderId - an ID
     * @param timestamp - the time of the message
     */
    public ChatMessageModel(String message, String senderId, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    /**
     * Getting for message attribute
     * @return message attribute
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for message attribute
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter for sender ID
     * @return sender ID attribute
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Setter for sender ID
     * @param senderId
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Getter for timestamp
     * @return timestamp attribute
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
     * Getter for isRead
     * @return ttrue if the message has been read, false otherwise
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Setter for isRead
     * @param read - true if message is read, false if unread
     */
    public void setRead(boolean read) {
        isRead = read;
    }
}
