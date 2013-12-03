package com.graphbrain.db;


import java.util.LinkedList;
import java.util.List;

public class SearchInterface {

    private Graph graph;

    public SearchInterface(Graph graph) {
        this.graph = graph;
    }

    public List<String> query(String text) {
        String id = ID.sanitize(text);
        int maxId = 0;

        while (graph.exists("" + (maxId + 1) + "/" + id))
            maxId += 1;

        List<String> res = new LinkedList<String>();
        for (int i = 1; i <= maxId; i++)
            res.add("" + i + "/" + id);

        return res;
    }
}
