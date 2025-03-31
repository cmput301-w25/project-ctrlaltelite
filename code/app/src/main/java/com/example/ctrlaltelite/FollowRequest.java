package com.example.ctrlaltelite;

/**
 * Follow Request Object Class
 */
public class FollowRequest {
    private String requesterUserName;
    private String requestedUserName;
    private String status;
    private String documentId;
    private String requestedDisplayName;
    private String requesterDisplayName;

    /**
     * Constructor
     * @param requesterUserName - the username of person requesting
     * @param requestedUserName - the username of person being requested to
     * @param requesterDisplayName - display name of person requesting
     * @param requestedDisplayName - display name of person being requested to
     * @param status - status of request
     */
    public FollowRequest(String requesterUserName, String requestedUserName, String requesterDisplayName,
                         String requestedDisplayName, String status) {
        this.requestedUserName = requestedUserName;
        this.requesterUserName = requesterUserName;
        this.requestedDisplayName = requestedDisplayName;
        this.requesterDisplayName = requesterDisplayName;
        this.status = status;
    }

    /**
     * Getter for requester user name
     * @return the username of person requesting
     */
    public String getRequesterUserName() {
        return requesterUserName;
    }

    /**
     * setter for requester user name
     * @param requesterUserName
     */
    public void setRequesterUserName(String requesterUserName) {
        this.requesterUserName = requesterUserName;
    }

    /**
     * Getter for requested user name
     * @return the user name of person being requested to
     */
    public String getRequestedUserName() {
        return requestedUserName;
    }

    /**
     * Setter for requested user name
     * @param requestedUserName
     */
    public void setRequestedUserName(String requestedUserName) {
        this.requestedUserName = requestedUserName;
    }

    /**
     * Getter for requester's display name
     * @return the display name of person being requested to
     */
    public String getRequestedDisplayName() {
        return requestedDisplayName;
    }

    /**
     * Setter for requested's display name
     * @param requestedDisplayName
     */
    public void setRequestedDisplayName(String requestedDisplayName) {
        this.requestedDisplayName = requestedDisplayName;
    }

    /**
     * Getter for requester's user name
     * @return username of person requested
     */
    public String getRequesterDisplayName() {
        return requesterDisplayName;
    }

    /**
     * Setter for requester's user name
     * @param requesterDisplayName
     */
    public void setRequesterDisplayName(String requesterDisplayName) {
        this.requesterDisplayName = requesterDisplayName;
    }

    /**
     * Getter for status
     * @return status of request
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Setter for status
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter for documentID
     * @return documentID in db collection
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Setter for documentID
     * @param documentId
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
