package com.graphbrain.db;

import java.util.Map;

public class ContextNode extends Textual {
  
	public String access;
	
	public ContextNode(String userId, String name, String access) {
		super(ID.buildContextId(userId, name));
		this.access = access;
	}
	
	public ContextNode(String userId, String name) {
		super(ID.buildContextId(userId, name));
		this.access = "public";
	}
	
	public ContextNode(String id, Map<String, String> map) {
		super(id, map);
		this.access = map.get("access");
		this.summary = map.get("summary");
	}
	
	@Override
	public VertexType type() {return VertexType.Context;}
	
	@Override
	public void fillMap(Map<String, String> map) {
		map.put("access", access);
		map.put("summary", summary);
	}
  
  	public String raw() {
  		return "type: " + "context<br />" +
  				"id: " + id + "<br />" +
  				"access: " + access + "<br />" +
  				"summary: " + summary + "<br />";
  	}
}