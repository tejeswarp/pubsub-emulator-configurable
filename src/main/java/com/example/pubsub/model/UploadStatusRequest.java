package com.example.pubsub.model;

public class UploadStatusRequest {
    private String partyId;
    private String fileNetId;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getFileNetId() {
        return fileNetId;
    }

    public void setFileNetId(String fileNetId) {
        this.fileNetId = fileNetId;
    }
}
