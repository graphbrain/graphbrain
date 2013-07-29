package com.graphbrain.db;

import java.util.List;

public interface Backend {
	public abstract void close();
	public abstract Vertex get(String id, VertexType type);
	public abstract Vertex put(Vertex vertex);
	public abstract Vertex update(Vertex vertex);
	public abstract void remove(Vertex vertex);
	public abstract void associateEmailToUsername(String email, String username);
	public abstract String usernameByEmail(String email);
	public abstract List<Vertex> listByType(VertexType type);
}