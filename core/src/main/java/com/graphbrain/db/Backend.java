package com.graphbrain.db;

//import VertexType.VertexType;

import java.util.List;
import java.util.Set;

interface Backend {
    public void close();
	public Vertex get(String id, VertexType vtype);
	public Vertex put(Vertex vertex);
    public Vertex update(Vertex vertex);
	public void remove(Vertex vertex);
	public void associateEmailToUsername(String email, String username);
	public String usernameByEmail(String email);
	public List<Vertex> listByType(VertexType vtype);
    public Set<Edge> edges(Edge pattern);
	public Set<Edge> edges(Vertex center);
    public void addLinkToGlobal(String globalId, String userId);
    public void removeLinkToGlobal(String globalId, String userId);
    public Set<String> alts(String globalId);
}