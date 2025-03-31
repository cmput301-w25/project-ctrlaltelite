package com.example.ctrlaltelite;

import com.google.firebase.Timestamp;

import java.util.List;


/**
 * Creating the chatroom model object
 */
public class ChatroomModel {
    String chatroomId;
    List<String> userIds;
    Timestamp lastMessageTimestamp;
    String lastMessageSenderId;
    String lastMessage;

    /**
     * Empty constructor
     */
    public ChatroomModel() {
    }

    /**
     * Constructor
     * @param chatroomId - ID of the chatroom
     * @param userIds - IDs of the user's
     * @param lastMessageTimestamp - timestamp of last message
     * @param lastMessageSenderId - the Id of last message
     */
    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    /**
     * Getter for chatroom ID
     * @return chatroom ID
     */
    public String getChatroomId() {
        return chatroomId;
    }

    /**
     * Setter for chatroom ID
     * @param chatroomId
     */
    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    /**
     * Getter for list of user IDS
     * @return list of User ids
     */
    public List<String> getUserIds() {
        return userIds;
    }

    /**
     * Setter for list of user IDs
     * @param userIds - ArrayList<String>
     */
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    /**
     * Getter for the timestamp of the last message
     * @return the timestamp
     */
    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    /**
     * Setter for timestamp of last message
     * @param lastMessageTimestamp
     */
    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    /**
     * Getter for last message Id
     * @return last message Id
     */
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    /**
     * Setter for last message ID
     * @param lastMessageSenderId
     */
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    /**
     * Getter for last message
     * @return the last message of the chat
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Setter for last message
     * @param lastMessage
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

}
