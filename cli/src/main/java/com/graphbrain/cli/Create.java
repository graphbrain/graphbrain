package com.graphbrain.cli;

import com.graphbrain.db.Vertex;

public class Create implements Command {
    public void run(String[] params) {
        String id = params[0];
        if (CLI.graph.exists(id)) {
            System.out.println(id + " already exists.");
        }
        else {
            Vertex v = Vertex.fromId(id);
            CLI.graph.put(v);
            System.out.println(id + " created.");
        }
    }
}
