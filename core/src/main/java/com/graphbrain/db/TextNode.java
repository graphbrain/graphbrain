package com.graphbrain.db;

import java.util.Map;

public class TextNode extends Vertex {

    private String text;

    @Override
    public VertexType type() {return VertexType.Text;}

    public TextNode(String id, String text, int degree, long ts) {
        super(id, degree, ts);
        this.text = text;
    }

    public TextNode(String id, String text) {
        this(id, text, 0, -1);
    }

    @Override
    public Vertex copy() {
        return new TextNode(id, text, degree, ts);
    }

    @Override
    protected void fillMap(Map<String, String> map) {
        map.put("text", text);
    }

    public String getText() {
        return text;
    }
}