package com.graphbrain.db;

import java.util.Map;

public class SourceNode extends Vertex {

	public SourceNode(String id) {
		super(id);
	}
  
	public SourceNode(String id, Map<String, String> map) {
		super(id, map);
	}
	
	@Override
	public VertexType type() {return VertexType.Source;}
}