package com.graphbrain.db;

import java.util.Map;

public class EdgeType extends Vertex {

    private String label;

    @Override
    public VertexType type() {return VertexType.EdgeType;}

    public EdgeType(String id, String label, int degree, long ts) {
        super(id, degree, ts);
        this.label = label;
    }

    public EdgeType(String id, String label) {
        this(id, label, 0, -1);
    }

    public EdgeType(String id) {
        this(id, "");
    }

    @Override
    public Vertex copy() {
        return new EdgeType(id, label, degree, ts);
    }

    public static boolean isNegative(String id) {
        return ID.parts(id)[0].equals("neg");
    }

    public boolean isNegative() {
        return isNegative(id);
    }

    public String getLabel() {
        return label;
    }
}