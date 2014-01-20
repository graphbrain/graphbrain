package com.graphbrain.db;

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

    public static String buildId(String text) {
        return "r/" + ID.sanitize(text);
    }

    public static String label(String id) {
        String lastPart = ID.lastPart(id);

        if (lastPart.equals("+pos")) {
            return "is";
        }
        else if (lastPart.equals("+can_mean")) {
            return "can mean";
        }
        else if (lastPart.equals("+type_of")) {
            return "is type of";
        }
        else if (lastPart.equals("+synonym")) {
            return "is synonym of";
        }
        else if (lastPart.equals("+part_of")) {
            return "is part of";
        }
        else if (lastPart.equals("+antonym")) {
            return "is opposite of";
        }
        else if (lastPart.equals("+also_see")) {
            return "related to";
        }

        return lastPart.replace("_", " ");
    }
}