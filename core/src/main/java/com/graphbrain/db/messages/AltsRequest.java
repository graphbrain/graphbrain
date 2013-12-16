package com.graphbrain.db.messages;


public class AltsRequest {
    private String globalId;

    public AltsRequest(String globalId) {
        this.globalId = globalId;
    }

    public AltsRequest() {
        this.globalId = "";
    }

    public String getGlobalId() {
        return globalId;
    }
}
