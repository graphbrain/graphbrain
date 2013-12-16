package com.graphbrain.db.messages;

public class RemoveLinkToGlobalRequest {
    private String globalId;
    private String userId;

    public RemoveLinkToGlobalRequest(String globalId, String userId) {
        this.globalId = globalId;
        this.userId = userId;
    }

    public RemoveLinkToGlobalRequest() {
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