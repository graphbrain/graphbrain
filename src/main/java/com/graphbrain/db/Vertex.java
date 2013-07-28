package com.graphbrain.db;

import java.util.Map;


public abstract class Vertex {
	protected String id;
	
	public Vertex(String id) {
		this.id = id;
	}
	
	public abstract Map<String, String> toMap();
	public abstract VertexType type();

	public Vertex toGlobal() {
		return this;
	}

	public Vertex toUser(String newUserId) {
		return this;
	}

	public Vertex removeContext() {
		return this;
	}

	public Vertex setContext(String newContext) {
		return this;
	}

	@Override
	public String toString() {
		return id;
	}

	public String description() {
		return toString();
	}

	public String raw() {
		return "";
	}

	public boolean shouldUpdate() {
		//!store.exists(id);
		return false;
	}

	public Vertex updateFromEdges() {
		return this;
	}
	
	public static String cleanId(String id) {
		return id.toLowerCase();
	}
	
	public String getId() {
		return id;
	}
}