package com.graphbrain.db;

import java.util.Arrays;
import java.util.Map;

public class Edge extends Vertex {

    private Vertex[] elems;
    private String[] ids;
    private String edgeType;
    private String[] participantIds;

    @Override
    public VertexType type() {return VertexType.Edge;}

    public Edge(Vertex[] elems, int degree, long ts) {
        super(buildId(elems), degree, ts);
        init(elems);
    }

    public Edge(Vertex[] elems) {
        this(elems, 0, -1);
    }

    public Edge(String id, Map<String, String> map) {
        super(id, map);
        Edge e = fromId(id);
        elems = e.elems;
        ids = e.ids;
        edgeType = e.edgeType;
        participantIds = e.participantIds;
    }

    @Override
    public Vertex copy() {
        return new Edge(elems, degree, ts);
    }

    private void init(Vertex[] elems) {
        this.elems = elems;
        ids = new String[elems.length];
        for (int i = 0; i < elems.length; i++) {
            ids[i] = elems[i].toString();
        }
        edgeType = ids[0];
        participantIds = Arrays.copyOfRange(ids, 1, ids.length);
    }

    public Edge negate() {
        return fromParticipants("neg/" + edgeType, participantIds);
    }

    public boolean isNegative() {
        return EdgeType.isNegative(edgeType);
    }

    public boolean isPositive() {
        return !isNegative();
    }

    public boolean isGlobal() {
        for (String p : participantIds) {
            if (!ID.isUserNode(p) && ID.isInUserSpace(p))
                return false;
        }

        return true;
    }

    public boolean isInUserSpace() {
        for (String p : participantIds)
            if (ID.isInUserSpace(p))
                return true;

        return false;
    }

    @Override
    public Vertex toUser(String userId) {
        String[] pids = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            pids[i] = ID.globalToUser(ids[i], userId);
        }
        return Edge.fromParticipants(pids);
    }

    @Override
    public Vertex toGlobal() {
        String[] pids = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            pids[i] = ID.userToGlobal(ids[i]);
        }
        return Edge.fromParticipants(pids);
    }

    public boolean matches(Edge pattern) {
        for (int i = 0; i < ids.length; i++)
            if ((!pattern.ids[i].equals("*")) && (!pattern.ids[i].equals(ids[i])))
                return false;

        return true;
    }

    public String humanReadable2() {
        return (ID.humanReadable(participantIds[0])
                + " [" +  ID.humanReadable(edgeType) + "] "
                + ID.humanReadable(participantIds[1])).replace(",", "");
    }

    public static String buildId(Object[] elems) {
        StringBuilder sb = new StringBuilder(50);
        sb.append("(");

        for (int i = 0; i < elems.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(elems[i].toString());
        }

        sb.append(")");

        return sb.toString();
    }

    public static Edge fromId(String id, int degree, long ts) {
        Vertex v = EdgeParser.parse(id);

        if (v instanceof Edge) {
            Edge e = (Edge)v;
            e.degree = degree;
            e.ts = ts;
            return e;
        }

        return null;
    }

    public static Edge fromId(String id) {
        return fromId(id, 0, -1);
    }

    public static Vertex[] elemsFromId(String id) {
        return fromId(id).elems;
    }

    public static Edge fromParticipants(String[] participants) {
        Vertex[] elems = new Vertex[participants.length];

        for (int i = 0; i < participants.length; i++) {
            elems[i] = Vertex.fromId(participants[i]);
        }
        return new Edge(elems);
    }

    public static Edge fromParticipants(String edgeType, String[] participantIds) {
        String[] parts = new String[participantIds.length + 1];
        parts[0] = edgeType;
        for (int i = 0; i < participantIds.length; i++) {
            parts[i + 1] = participantIds[i];
        }

        return fromParticipants(parts);
    }

    public String[] getIds() {
        return ids;
    }

    public String[] getParticipantIds() {
        return participantIds;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public Vertex[] getElems() {
        return elems;
    }
}