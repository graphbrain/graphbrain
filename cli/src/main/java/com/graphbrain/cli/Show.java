package com.graphbrain.cli;

import com.graphbrain.db.Vertex;

public class Show implements Command {

    public void run(String[] params) {
        String id = params[0];
        Vertex v = CLI.graph.get(id);
        if (v == null) {
            System.out.println("vertex " + id + " does not exist.");
        }
        else {
            System.out.println(v.info());
        }
    }
}
