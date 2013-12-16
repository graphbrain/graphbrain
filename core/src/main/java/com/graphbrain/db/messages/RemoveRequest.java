package com.graphbrain.db.messages;

import com.graphbrain.db.Vertex;

public class RemoveRequest {
    private Vertex vertex;

    public RemoveRequest(Vertex vertex) {
        this.vertex = vertex;
    }

    public RemoveRequest() {
        this.vertex = null;
    }

    public Vertex getVertex() {
        return vertex;
    }
}