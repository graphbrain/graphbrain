package com.graphbrain.db;

import java.util.HashMap;
import java.util.Map;


public class EdgeType extends Vertex {

	private String label;
			
	public EdgeType(String id, String label) {
		super(id);
		this.label = label;
	}
	
	public EdgeType(String id, Map<String, String> map) {
		super(id);
		this.label = map.get("label");
	}
	
	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("label", label);
		return map;
	}
}