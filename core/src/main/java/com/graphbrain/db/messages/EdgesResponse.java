package com.graphbrain.db.messages;

import com.graphbrain.db.Edge;

import java.util.Set;

public class EdgesResponse {
    private Set<Edge> vertices;

    public EdgesResponse(Set<Edge> edges) {
        this.vertices = edges;
    }

    public EdgesResponse() {
        this.vertices = null;
    }

    public Set<Edge> getVertices() {
        return vertices;
    }
}