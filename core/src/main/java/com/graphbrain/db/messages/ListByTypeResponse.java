package com.graphbrain.db.messages;

import com.graphbrain.db.Vertex;

import java.util.List;

public class ListByTypeResponse {
    private List<Vertex> vertices;

    public ListByTypeResponse() {
        vertices = null;
    }

    public ListByTypeResponse(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }
}
