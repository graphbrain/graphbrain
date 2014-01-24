package com.graphbrain.db;

public class TextNode extends Vertex {

    private String text;

    @Override
    public VertexType type() {return VertexType.Text;}

    public TextNode(String id, String text, int degree, long ts) {
        super(id, degree, ts);
        this.text = text;
    }

    public TextNode(String text, int degree, long ts) {
        this(idFromText(text), text, degree, ts);
    }

    public TextNode(String id, String text) {
        this(id, text, 0, -1);
    }

    @Override
    public Vertex copy() {
        return new TextNode(id, text, degree, ts);
    }

    public String getText() {
        return text;
    }

    public static String idFromText(String text) {
        String shortText = text.substring(0, Math.min(text.length(), 50));
        String hash = ID.hash(text);
        return hash + "/" + ID.sanitize(shortText);
    }
}