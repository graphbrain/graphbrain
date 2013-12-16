package com.graphbrain.db.messages;

import com.graphbrain.db.Vertex;

public class UpdateRequest {
    private Vertex vertex;

    public UpdateRequest(Vertex vertex) {
        this.vertex = vertex;
    }

    public UpdateRequest() {
        this.vertex = null;
    }

    public Vertex getVertex() {
        return vertex;
    }
}
