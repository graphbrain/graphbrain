package com.graphbrain.db.messages;

import com.graphbrain.db.Edge;
import com.graphbrain.db.Vertex;

public class EdgesRequest {
    private Vertex vertex;
    private Edge edge;

    public EdgesRequest(Vertex vertex) {
        this.vertex = vertex;
        this.edge = null;
    }

    public EdgesRequest(Edge edge) {
        this.vertex = null;
        this.edge = edge;
    }

    public EdgesRequest() {
        this.vertex = null;
        this.edge = null;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public Vertex getEdge() {
        return edge;
    }
}
