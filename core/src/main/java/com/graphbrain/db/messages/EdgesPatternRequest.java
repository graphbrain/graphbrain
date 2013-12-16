package com.graphbrain.db.messages;

import com.graphbrain.db.Edge;
import com.graphbrain.db.Vertex;

public class EdgesPatternRequest {
    private Edge edge;

    public EdgesPatternRequest(Edge edge) {
        this.edge = edge;
    }

    public EdgesPatternRequest() {
        this.edge = null;
    }

    public Vertex getEdge() {
        return edge;
    }
}
