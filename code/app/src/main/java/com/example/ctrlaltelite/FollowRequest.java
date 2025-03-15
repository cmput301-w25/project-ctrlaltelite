package com.example.ctrlaltelite;

public class FollowRequest {
    private User requester;
    private User requestedUser;
    private String status;
    private String documentId;

    public FollowRequest(User requester, User requestedUser, String status) {
        this.requester = requester;
        this.requestedUser = requestedUser;
        this.status = status;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getRequestedUser() {
        return requestedUser;
    }

    public void setRequestedUser(User requestedUser) {
        this.requestedUser = requestedUser;
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
