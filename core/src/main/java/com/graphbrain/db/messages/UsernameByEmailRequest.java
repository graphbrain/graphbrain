package com.graphbrain.db.messages;


public class UsernameByEmailRequest {
    private String email;

    public UsernameByEmailRequest(String email) {
        this.email = email;
    }

    public UsernameByEmailRequest() {
        this.email = "";
    }

    public String getEmail() {
        return email;
    }
}
