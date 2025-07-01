package com.example.pubsub.model;

public class UploadDocRequest {
    private String partyId;
    private String fileName;

    public String getPartyId() { return partyId; }
    public void setPartyId(String partyId) { this.partyId = partyId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
