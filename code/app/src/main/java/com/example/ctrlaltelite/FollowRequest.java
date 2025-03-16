package com.example.ctrlaltelite;

public class FollowRequest {
    private String requesterUserName;
    private String requestedUserName;
    private String status;
    private String documentId;
    private String requestedDisplayName;
    private String requesterDisplayName;

    public FollowRequest(String requesterUserName, String requestedUserName, String requesterDisplayName,
                         String requestedDisplayName, String status) {
        this.requestedUserName = requestedUserName;
        this.requesterUserName = requesterUserName;
        this.requestedDisplayName = requestedDisplayName;
        this.requesterDisplayName = requesterDisplayName;
        this.status = status;
    }

    public String getRequesterUserName() {
        return requesterUserName;
    }

    public void setRequesterUserName(String requesterUserName) {
        this.requesterUserName = requesterUserName;
    }

    public String getRequestedUserName() {
        return requestedUserName;
    }

    public void setRequestedUserName(String requestedUserName) {
        this.requestedUserName = requestedUserName;
    }

    public String getRequestedDisplayName() {
        return requestedDisplayName;
    }

    public void setRequestedDisplayName(String requestedDisplayName) {
        this.requestedDisplayName = requestedDisplayName;
    }

    public String getRequesterDisplayName() {
        return requesterDisplayName;
    }

    public void setRequesterDisplayName(String requesterDisplayName) {
        this.requesterDisplayName = requesterDisplayName;
    }

    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
