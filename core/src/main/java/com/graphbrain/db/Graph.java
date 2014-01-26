package com.graphbrain.db;

import java.util.*;

public class Graph {
    private Backend back;

    public Graph(String name) {
        back = new MySqlBackend(name);
    }

    public Graph() {
        this("gbnode");
    }

    public Vertex get(String id) {
        return back.get(id, VertexType.getType(id));
    }

    public Vertex put(Vertex vertex) {

        if (!exists(vertex)) {
            if (vertex.ts < 0) {
                vertex.setTs((new Date()).getTime());
            }

            back.put(vertex);

            if (vertex.type() == VertexType.Edge) {
                Edge edge = (Edge)vertex;
                for (String id : edge.getIds()) {
                    put(Vertex.fromId(id));
                    incDegree(id);
                }
            }
        }

        return vertex;
    }

    public Vertex put(Vertex vertex, String userid) {
        //logger.debug(s"put $vertex; userId: $userid")

        // create participant vertices if edge
        if (vertex.type() == VertexType.Edge) {
            Edge edge = (Edge)vertex;

            for (String id : edge.getIds()) {
                put(Vertex.fromId(id), userid);
            }
        }

        put(vertex);

        if (!ID.isInUserSpace(vertex.id)) {
            Vertex uVertex = vertex.toUser(userid);
            //logger.debug(s"uVertex: $uVertex")
            if (!exists(uVertex)) {
                put(uVertex);
                addLinkToGlobal(vertex.id, uVertex.id);
            }
        }

        if (vertex.type() == VertexType.Edge) {
            Edge edge = (Edge)vertex;

            remove(edge.negate());

            // run consensus algorithm
            Edge gEdge = (Edge)edge.toGlobal();
            Consensus.evalEdge(gEdge, this);
        }

        return vertex;
    }

    public Vertex update(Vertex vertex) {
        return back.update(vertex);
    }

    public boolean exists(String id) {
        return back.exists(id, VertexType.getType(id));
    }

    public boolean exists(Vertex v) {
        return exists(v.id);
    }

    public void remove(Vertex vertex) {
        back.remove(vertex);

        if (vertex.type() == VertexType.Edge)
            onRemoveEdge((Edge)vertex);
    }

    public void connectVertices(String[] participants) {
        //logger.debug(s"connectVertices participants: ${participants.mkString(" ")}")
        put(Edge.fromParticipants(participants));
    }

    public Set<Edge> edges(Edge pattern) {
        return back.edges(pattern);
    }

    public Set<Edge> edges(Vertex center) {
        return back.edges(center);
    }

    public Set<Edge> edges(String centerId) {
        return edges(get(centerId));
    }

    public Set<Edge> edges(String[] pattern) {
        return back.edges(Edge.fromParticipants(pattern));
    }

    public Set<String> nodesFromEdgeSet(Set<Edge> edgeSet) {
        Set<String> nset = new HashSet<>();

        for (Edge e : edgeSet) {
            nset.addAll(Arrays.asList(e.getIds()));
        }

        return nset;
    }

    public Set<String> neighbors(String centerId) {
        //logger.debug(s"neighbors: $centerId")

        Set<Edge> nedges = edges(centerId);
        Set<String> nodes = nodesFromEdgeSet(nedges);
        nodes.add(centerId);
        return nodes;
    }

    protected Vertex incDegree(Vertex vertex) {
        return update(vertex.setDegree(vertex.getDegree() + 1));
    }

    protected Vertex incDegree(String id) {
        return incDegree(get(id));
    }

    protected Vertex decDegree(Vertex vertex) {
        return update(vertex.setDegree(vertex.degree - 1));
    }

    protected Vertex decDegree(String id) {
        Vertex v = get(id);
        if (v != null)
            return decDegree(v);
        else
            return null;
    }

    protected void onRemoveEdge(Edge edge) {
        for (String id : edge.getIds())
            decDegree(id);
    }

    public String description(Vertex vertex) {
        Set<Edge> asIn = edges(new String[]{"r/+type_of", vertex.id, "*"});

        String desc = vertex.label();

        if (asIn.size() > 0) {
            desc += " (";

            boolean first = true;
            for (Edge e : asIn) {
                if (first)
                    first = false;
                else
                    desc += ", ";
                desc += get(e.getParticipantIds()[1]).label();
            }

            desc += ")";
        }

        return desc;
    }

    public String description(String id) {
        return description(get(id));
    }

    public TextNode getTextNode(String id) {
        Vertex v = get(id);

        if (v == null)
            return null;

        if (v.type() == VertexType.Text) {
            return (TextNode)v;
        }
        else {
            return null;
        }
    }

    public ProgNode getProgNode(String id) {
        Vertex v = get(id);

        if (v == null)
            return null;

        if (v.type() == VertexType.Prog) {
            return (ProgNode)v;
        }
        else {
            return null;
        }
    }

    // User management

    public UserNode getUserNode(String id) {
        Vertex v = get(id);

        if (v == null)
            return null;

        if (v.type() == VertexType.User) {
            return (UserNode)v;
        }
        else {
            return null;
        }
    }

    protected String idFromEmail(String email) {
        String userName = back.usernameByEmail(email);
        if (userName == null)
            return null;
        else
            return ID.idFromUsername(userName);
    }

    public boolean usernameExists(String username) {
        return exists(ID.idFromUsername(username));
    }

    public boolean emailExists(String email) {
        return back.usernameByEmail(email) != null;
    }

    public UserNode findUser(String login) {
        if (exists(ID.idFromUsername(login))) {
            return getUserNode(ID.idFromUsername(login));
        }
        else {
            String uid = idFromEmail(login);
            if (uid == null)
                return null;
            else if (exists(uid))
                return getUserNode(idFromEmail(login));
            else
                return null;
        }
    }

    public UserNode getUserNodeByUsername(String username) {
        if (exists(ID.idFromUsername(username)))
            return getUserNode(ID.idFromUsername(username));
        else
            return null;
    }

    public UserNode createUser(String username, String name, String email, String password, String role) {
        UserNode userNode = UserNode.create(username, name, email, password, role);
        back.put(userNode);
        if (!email.isEmpty())
            back.associateEmailToUsername(email, username);

        return userNode;
    }

    public UserNode attemptLogin(String login, String password) {
        UserNode userNode = findUser(login);

        // user does not exist
        if (userNode == null) {
            return null;
        }

        // password is incorrect
        if (!userNode.checkPassword(password)) {
            return null;
        }

        // ok, create new session
        UserNode un = userNode.newSession();
        update(un);
        return un;
    }

    public UserNode forceLogin(String login) {
        UserNode userNode = findUser(login);

        // user does not exist
        if (userNode == null)
            return null;

        // ok, create new session
        UserNode un = userNode.newSession();
        update(un);
        return un;
    }

    public List<UserNode> allUsers() {
        return back.allUsers();
    }

    // User Ops

    private void addLinkToGlobal(String globalId, String userId) {
        //logger.debug(s"addLinkToGlobal globalNodeId: $globalId; userNodeId: $userId")
        back.addLinkToGlobal(globalId, userId);
    }

    private void removeLinkToGlobal(String globalId, String userId) {
        //logger.debug(s"removeLinkToGlobal globalNodeId: $globalId; userNodeId: $userId")
        back.removeLinkToGlobal(globalId, userId);
    }

    public Set<String> globalAlts(String globalId) {
        //ldebug("globalAlts: " + globalId)
        return back.alts(globalId);
    }

    public void remove(Vertex vertex, String userId) {

        Vertex u = vertex.toUser(userId);

        // delete from user space
        remove(u);
        removeLinkToGlobal(vertex.id, u.id);

        if (vertex.type() == VertexType.Edge) {
            Edge e = (Edge)vertex;
            // create negation of edge in user space
            put(e.negate());
            // run consensus algorithm
            Consensus.evalEdge(e, this);
        }
    }

    public Vertex getOrInsert(Vertex node, String userId) {
        //logger.debug(s"getOrInsert: $node; userId: $userId")
        Vertex g = get(node.id);
        if (g == null) {
            g = put(node);
        }
        Vertex u = get(ID.globalToUser(node.id, userId));

        if (u == null) {
            put(node, userId);
            return get(node.id);
        }
        else {
            return g;
        }
    }

    public Edge createAndConnectVertices(Vertex[] participants, String userId) {
        //logger.debug(s"createAndConnectVertices participants: ${participants.map(_.id).mkString(" ")}; userId: $userId")
        for (Vertex v : participants) {
            String uid = ID.globalToUser(v.id, userId);
            if (!exists(uid)) {
                put(v, userId);
            }
        }
        return (Edge)put(new Edge(participants), userId);
    }

    public Edge connectVertices(String[] participants, String userId) {
        //logger.debug(s"connectVertices participants: ${participants.mkString(" ")}; userId: $userId")
        return (Edge)put(Edge.fromParticipants(participants), userId);
    }

    public Set<Edge> edges(String centerId, String userId) {
        //logger.debug(s"edges centerId: $centerId; userId: $userId")

        Set<Edge> edges = edges(centerId);

        Set<Edge> gedges = new HashSet<>();
        for (Edge e : edges) {
            if (e.isGlobal())
                gedges.add(e);
        }

        Set<Edge> uedges = new HashSet<>();

        if (userId != null) {
            String uCenterId = ID.globalToUser(centerId, userId);

            edges = edges(uCenterId);

            for (Edge e : edges) {
                if (e.isInUserSpace())
                    uedges.add((Edge)e.toGlobal());
            }
        }

        edges = new HashSet<>();

        for (Edge e : gedges) {
            if (!uedges.contains(e.negate())) {
                edges.add(e);
            }
        }

        for (Edge e : uedges) {
            if (e.isPositive()) {
                edges.add(e);
            }
        }

        return edges;
    }
}