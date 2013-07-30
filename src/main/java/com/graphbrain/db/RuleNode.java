package com.graphbrain.db;

import java.util.Map;

public class RuleNode extends Vertex {
  
	public RuleNode(String id) {
		super(id);
	}
  
	public RuleNode(String id, Map<String, String> map) {
		super(id, map);
	}
	
	@Override
	public VertexType type() {return VertexType.Rule;}
}