package com.graphbrain.db.messages;


public class AddLinkToGlobalRequest {
    private String globalId;
    private String userId;

    public AddLinkToGlobalRequest(String globalId, String userId) {
        this.globalId = globalId;
        this.userId = userId;
    }

    public AddLinkToGlobalRequest() {
        this.globalId = "";
        this.userId = "";
    }

    public String getGlobalId() {
        return globalId;
    }

    public String getUserId() {
        return userId;
    }
}
