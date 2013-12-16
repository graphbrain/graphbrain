package com.graphbrain.db;

import java.util.Map;

public class EntityNode extends Vertex {

    @Override
    public VertexType type() {return VertexType.Entity;}

    public EntityNode(String id, int degree, long ts) {
        super(id, degree, ts);
    }

    public EntityNode(String id) {
        this(id, 0, -1);
    }

    public EntityNode(String id, Map<String, String> map) {
        super(id, map);
    }

    public EntityNode() {}

    @Override
    public Vertex copy() {
        return new EntityNode(id, degree, ts);
    }

    public String text() {
        return ID.lastPart(id).replace("_", " ");
    }

    public static String id(String namespace, String text) {
        return namespace + "/" + ID.sanitize(text).toLowerCase();
    }

    public static EntityNode fromNsAndText(String namespace, String text) {
        return new EntityNode(id(namespace, text));
    }

    @Override
    public String raw() {
        return "type: "
                + "text<br />"
                + "id: "
                + id
                + "<br />";
    }
}