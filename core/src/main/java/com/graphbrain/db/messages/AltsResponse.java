package com.graphbrain.db.messages;

import java.util.Set;

public class AltsResponse {
    private Set<String> alts;

    public AltsResponse() {
        alts = null;
    }

    public AltsResponse(Set<String> alts) {
        this.alts = alts;
    }

    public Set<String> getAlts() {
        return alts;
    }
}
