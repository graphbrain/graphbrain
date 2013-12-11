package com.graphbrain.web;

import com.graphbrain.db.*;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class HandleNode extends VelocityRoute {

    public static final int MAX_SNODES = 15;

    public HandleNode(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {

        String id = request.splat()[0];

        WebServer.log(request, "NODE id: " + id);

        UserNode userNode = WebServer.getUser(request);
        Vertex node;
        try {
            node = WebServer.graph.get(URLDecoder.decode(id, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String errorMsg = "";

        System.out.println("node: " + node);
        System.out.println("userNode: " + userNode);

        String js = "var data = " + generate(node.id, userNode) + ";\n" +
                    "var errorMsg = \"" + errorMsg + "\";\n";

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", node.toString());
        attributes.put("navBar", new NavBar(null, "node").html());
        attributes.put("cssAndJs", new CssAndJs().cssAndJs());
        attributes.put("loggedIn", false);
        attributes.put("js", js);


        return modelAndView(attributes, "velocity/template/node.wm");
    }

    private class RelPos {
        public String rel;
        public int pos;

        public RelPos(String rel, int pos) {
            this.rel = rel;
            this.pos = pos;
        }

        public String snodeId() {
            return rel.replaceAll("/", "_").replaceAll(" ", "_").replaceAll("\\.", "_") + "_" + pos;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 31).append(rel).append(pos).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (!(obj instanceof RelPos))
                return false;

            RelPos rp = (RelPos)obj;
            return (pos == rp.pos) && (rel.equals(rp.rel));
        }
    }

    private String generate(String rootId, UserNode user) {
        String userId = "";
        if (user != null)
            userId = user.id;

        // get neighboring edges
        Set<Edge> hyperEdges = WebServer.graph.edges(rootId, userId);

        // map hyperedges to visual edges
        Set<SimpleEdge> visualEdges = new HashSet<SimpleEdge>();
        for (Edge e: hyperEdges) {
            if (e.isPositive()) {
                SimpleEdge se = hyper2edge(e, rootId);
                if (se != null) {
                    visualEdges.add(se);
                }
            }
        }

        // group nodes by edge type
        Map<RelPos, Set<SimpleEdge>> edgeNodeMap = generateEdgeNodeMap(visualEdges, rootId);

        // full relations list
        List<Map<String, String>> allRelations = new LinkedList<Map<String, String>>();
        for (RelPos rp : edgeNodeMap.keySet()) {
            Map<String, String> r = new HashMap<String, String>();
            r.put("rel", rp.rel);
            r.put("pos", "" + rp.pos);
            r.put("label", linkLabel(rp.rel));
            r.put("snode", rp.snodeId());
            allRelations.add(r);
        }

        // create map with all information for supernodes
        Map<String, Map<String, Object>> snodeMap = generateSnodeMap(edgeNodeMap, rootId);

        // create reply structure with all the information needed for rendering
        JSONObject json = new JSONObject();
        json.put("user", userId);
        json.put("root", node2map(rootId, "", rootId));
        json.put("snodes", snodeMap);
        json.put("allrelations", allRelations);

        return json.toString();
    }

    private SimpleEdge hyper2edge(Edge edge, String rootId) {
        if (edge.getParticipantIds().length > 2) {
            if (edge.getEdgeType().equals("r/1/instance_of~owned_by")) {
                if (edge.getParticipantIds()[0].equals(rootId)) {
                    return new SimpleEdge("r/1/has", edge.getParticipantIds()[2], rootId, edge);
                }
                else if (edge.getParticipantIds()[2].equals(rootId)) {
                    return new SimpleEdge("r/1/has", rootId, edge.getParticipantIds()[0], edge);
                }
                else {
                    return null;
                }
            }
            if (edge.getEdgeType().equals("r/1/has~of_type")) {
                String edgeType = "has " + ID.lastPart(edge.getParticipantIds()[2]);
                return new SimpleEdge(edgeType, edge.getParticipantIds()[0], edge.getParticipantIds()[1], edge);
            }
            else {
                String[] parts = edge.getEdgeType().split("~");
                String edgeType = parts[0];
                edgeType += " ";
                edgeType += ID.lastPart(edge.getParticipantIds()[1]);
                for (int i = 1; i < parts.length; i++) {
                    edgeType += " ";
                    edgeType += parts[i];
                }

                return new SimpleEdge(edgeType, edge.getParticipantIds()[0], edge.getParticipantIds()[2], edge);
            }
        }
        else {
            return new SimpleEdge(edge);
        }
    }

    private void addToEdgeNodeMap(Map<RelPos, Set<SimpleEdge>> enMap, RelPos key, SimpleEdge e, String rootId) {
        if (key.pos == 0) {
            if (e.getId1().equals(rootId))
                return;
        }
        if (key.pos == 1) {
            if (e.getId2().equals(rootId))
                return;
        }

        if (!enMap.containsKey(key)) {
            enMap.get(key).add(e);
        }
        else {
            Set<SimpleEdge> set = new HashSet<SimpleEdge>();
            set.add(e);
            enMap.put(key, set);
        }
    }

    private Map<RelPos, Set<SimpleEdge>> generateEdgeNodeMap(Set<SimpleEdge> edges, String rootId) {
        Map<RelPos, Set<SimpleEdge>> enMap = new HashMap<RelPos, Set<SimpleEdge>>();

        int count = 0;
        for (SimpleEdge e : edges) {
            addToEdgeNodeMap(enMap, new RelPos(e.getEdgeType(), 0), e, rootId);
            addToEdgeNodeMap(enMap, new RelPos(e.getEdgeType(), 1), e, rootId);

            count++;
            if (count > MAX_SNODES) {
                return enMap;
            }
        }

        return enMap;
    }

    private Map<String, String> node2map(String nodeId, String nodeEdge, String rootId) {
        VertexType vtype = VertexType.getType(nodeId);
        Vertex node;

        if ((nodeId.equals(rootId)) && (vtype == VertexType.Entity)) {
            node = new EntityNode(nodeId);
        }
        else {
            node = WebServer.graph.get(nodeId);
        }

        Map<String, String> map = new HashMap<String, String>();

        switch(node.type()) {
            case Entity:
                EntityNode en = (EntityNode)node;
                map.put("id", en.id);
                map.put("type", "text");
                map.put("text", en.text());
                map.put("edge", nodeEdge);
                return map;
            case URL:
                URLNode un;
                if (node instanceof URLNode)
                    un = (URLNode)node;
                else
                    return map;
                String title;
                if (un.getTitle().equals(""))
                    title = un.getUrl();
                else
                    title = un.getTitle();
                map.put("id", un.id);
                map.put("type", "url");
                map.put("text", title);
                map.put("url", un.getUrl());
                map.put("icon", un.getIcon());
                map.put("edge", nodeEdge);
                return map;
            case User:
                UserNode us;
                if (node instanceof UserNode)
                    us = (UserNode)node;
                else
                    return map;
                map.put("id", us.id);
                map.put("type", "user");
                map.put("text", us.getName());
                map.put("edge", nodeEdge);
                return map;
            default:
                map.put("id", node.id);
                map.put("type", "text");
                map.put("text", node.id);
                map.put("edge", nodeEdge);
                return map;
        }
    }

    private Map<String, Object> generateSnode(RelPos rp, Set<SimpleEdge> sedges, String rootId) {
        String label = linkLabel(rp.rel);
        String color = linkColor(label);
        List<Map<String, String>> nodes = new LinkedList<Map<String, String>>();

        for (SimpleEdge se : sedges) {
            String nodeId;
            if (rp.pos == 0) {
                nodeId = se.getId1();
            }
            else {
                nodeId = se.getId2();
            }

            nodes.add(node2map(nodeId, se.getParent().id, rootId));
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("nodes", nodes);
        data.put("etype", rp.rel);
        data.put("rpos", rp.pos);
        data.put("label", label);
        data.put("color", color);

        return data;
    }

    private Map<String, Map<String, Object>> generateSnodeMap(Map<RelPos, Set<SimpleEdge>> edgeNodeMap, String rootId) {
        Map<String, Map<String, Object>> snodeMap = new HashMap<String, Map<String, Object>>();

        for (RelPos rp : edgeNodeMap.keySet()) {
            snodeMap.put(rp.snodeId(), generateSnode(rp, edgeNodeMap.get(rp), rootId));
        }

        return snodeMap;
    }

    private String linkColor(String label) {
        int index = Math.abs(label.hashCode()) % Colors.colors.length;
        return Colors.colors[index];
    }

    private String fixLabel(String label) {
        if (EdgeLabelTable.lt.containsKey(label))
            return EdgeLabelTable.lt.get(label);
        else
            return label;
    }

    private String linkLabel(String edgeType) {
        if (edgeType.isEmpty())
            return "";

        String lastPart = ID.lastPart(edgeType);
        return fixLabel(lastPart.replace("_", " "));
    }
}