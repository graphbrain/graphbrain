package com.graphbrain.cli;

import com.graphbrain.db.Edge;
import java.util.Set;

public class Edges implements Command {
    public void run(String[] params) {
        String id = params[0];
        Set<Edge> edges = CLI.graph.edges(id);

        for (Edge e: edges)
            System.out.println(e);
    }
}
