package com.graphbrain.db;

public class SimpleEdge {

    private String edgeType;
    private String id1;
    private String id2;
    private Edge parent;

    public SimpleEdge(String edgeType, String id1, String id2, Edge parent) {
        this.edgeType = edgeType;
        this.id1 = id1;
        this.id2 = id2;
        this.parent = parent;
    }

    public SimpleEdge(Edge edge) {
        this(edge.getIds()[0], edge.getIds()[1], edge.getIds()[2], edge);
    }

    public String getEdgeType() {
        return edgeType;
    }

    public String getId1() {
        return id1;
    }

    public String getId2() {
        return id2;
    }

    public Edge getParent() {
        return parent;
    }
}