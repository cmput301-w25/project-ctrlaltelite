package com.example.ctrlaltelite;

public class FollowRequest {
    private String requesterUserName;
    private String requestedUserName;
    private String status;
    private String documentId;

    public FollowRequest(String requesterUserName, String requestedUserName, String status) {
        this.requestedUserName = requestedUserName;
        this.requesterUserName = requesterUserName;
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
