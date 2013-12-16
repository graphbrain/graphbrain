package com.graphbrain.db.messages;


public class AssociateEmailToUsernameRequest {
    private String email;
    private String username;

    public AssociateEmailToUsernameRequest(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public AssociateEmailToUsernameRequest() {
        this.email = "";
        this.username = "";
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
