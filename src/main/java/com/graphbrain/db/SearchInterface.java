package com.graphbrain.db;


import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SearchInterface {

    private Graph graph;

    public SearchInterface(Graph graph) {
        this.graph = graph;
    }

    public String[] query(String text) {
        String id = ID.sanitize(text);
        Set<Edge> canMean = graph.edges(new String[]{"r/+can_mean", id, "*"});

        List<String> res = new LinkedList<>();

        for (Edge e : canMean) {
            res.add(e.getParticipantIds()[1]);
        }

        return res.toArray(new String[res.size()]);
    }
}
