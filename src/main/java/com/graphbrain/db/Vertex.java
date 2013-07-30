package com.graphbrain.db;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public abstract class Vertex {
	protected String id;
	protected long degree;
	protected long ts;
	
	public Vertex(String id) {
		this.id = id;
		degree = 0;
		ts = -1;
	}
	
	public Vertex(String id, Map<String, String> map) {
		this(id);
		degree = Long.parseLong(map.get("degree"));
		ts = Long.parseLong(map.get("ts"));
	}
	
	public abstract VertexType type();
	public void fillMap(Map<String, String> map) {}
	
	public final Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("degree", "" + degree);
		map.put("ts", "" + ts);
		fillMap(map);
		return map;
	}
	
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
	
	public long getDegree() {
		return degree;
	}
	
	public long getTimestamp() {
		return ts;
	}
	
	public void incDegree() {
		degree++;
	}
	
	public void decDegree() {
		degree--;
	}
	
	public void setTimestampNow() {
		ts = (new Date()).getTime();
	}
}