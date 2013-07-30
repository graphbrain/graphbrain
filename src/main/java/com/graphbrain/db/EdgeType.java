package com.graphbrain.db;

import java.util.Map;


public class EdgeType extends Vertex {

	private String label;
			
	public EdgeType(String id, String label) {
		super(id);
		this.label = label;
	}
	
	public EdgeType(String id, Map<String, String> map) {
		super(id, map);
		this.label = map.get("label");
	}
	
	@Override
	public VertexType type() {return VertexType.EdgeType;}
	
	@Override
	public void fillMap(Map<String, String> map) {
		map.put("label", label);
	}
}