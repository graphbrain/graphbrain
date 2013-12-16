package com.graphbrain.db.messages;

import com.graphbrain.db.Vertex;

public class PutRequest {
    private Vertex vertex;

    public PutRequest(Vertex vertex) {
        this.vertex = vertex;
    }

    public PutRequest() {
        this.vertex = null;
    }

    public Vertex getVertex() {
        return vertex;
    }
}
