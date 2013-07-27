package com.graphbrain.db;

import java.util.HashMap;
import java.util.Map;

public class RuleNode extends Vertex {
  
	public RuleNode(String id) {
		super(id);
	}
  
	public RuleNode(String id, Map<String, String> map) {
		super(id);
	}
	
	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}
}