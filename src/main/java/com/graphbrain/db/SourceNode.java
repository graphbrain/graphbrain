package com.graphbrain.db;

import java.util.HashMap;
import java.util.Map;

public class SourceNode extends Vertex {

	public SourceNode(String id) {
		super(id);
	}
  
	public SourceNode(String id, Map<String, String> map) {
		super(id);
	}
	
	@Override
	public VertexType type() {return VertexType.Source;}
	
	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}
}