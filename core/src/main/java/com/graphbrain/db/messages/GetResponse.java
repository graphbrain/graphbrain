package com.graphbrain.db.messages;

import com.graphbrain.db.Vertex;

public class GetResponse {
    private Vertex vertex;

    public GetResponse() {
        vertex = null;
    }

    public GetResponse(Vertex vertex) {
        this.vertex = vertex;
    }

    public Vertex getVertex() {


        return vertex;
    }
}
