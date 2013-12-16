package com.graphbrain.db.messages;


public class UsernameByEmailResponse {
    private String username;

    public UsernameByEmailResponse(String username) {
        this.username = username;
    }

    public UsernameByEmailResponse() {
        this.username = "";
    }

    public String getUsername() {
        return username;
    }
}
