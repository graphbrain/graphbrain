package com.graphbrain.db;


import java.util.Set;

public class Consensus {
    public static void evalEdge(Edge edge, Graph graph) {
        if (edge.isGlobal()) {
            int score = 0;

            Edge negEdge = edge.negate();
            String[] pids = edge.getParticipantIds();

            // go through all the user space alt version of the first version in this edge
            // TODO: select edge participant with the lowest degree?

            Set<String> altVertices = graph.globalAlts(pids[0]);
            for (String altV : altVertices) {
                String userId = ID.ownerId(altV);

                // generate edges with user space participants owned by this user id
                Edge userEdge = (Edge)edge.toUser(userId);
                Edge negUserEdge = (Edge)negEdge.toUser(userId);

                // update score based on postive and negative user space edges
                if (graph.exists(userEdge)) {
                    score += 1;
                }
                if (graph.exists(negUserEdge)) {
                    score -= 1;
                }
            }

            // add or delete edge based on consensus score
            if (score > 0) {
                graph.put(edge);
            }
            else {
                graph.remove(edge);
            }
        }
    }
}