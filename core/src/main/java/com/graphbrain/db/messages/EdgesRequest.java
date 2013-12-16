package com.graphbrain.db.messages;

import com.graphbrain.db.Vertex;

public class EdgesRequest {
    private Vertex vertex;

    public EdgesRequest(Vertex vertex) {
        this.vertex = vertex;
    }

    public EdgesRequest() {
        this.vertex = null;
    }

    public Vertex getVertex() {
        return vertex;
    }
}
