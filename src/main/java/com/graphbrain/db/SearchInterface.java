package com.graphbrain.db;

public class SearchInterface {
	
	private Graph graph;
	
	public SearchInterface(Graph graph) {
		this.graph = graph;
	}
	
	public String[] query(String text) {
		String id = ID.sanitize(text);
		int maxId = 0;

		while (graph.exists("" + (maxId + 1) + "/" + id)) {
			maxId += 1;
		}

		String res[] = new String[maxId];
		for (int i = 1; i <= maxId; i++) {
			res[i] = "" + i + "/" + id;
		}
		return res;
	}
}