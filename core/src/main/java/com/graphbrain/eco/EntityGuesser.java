package com.graphbrain.eco;

import com.graphbrain.db.*;

import java.util.Set;

public class EntityGuesser {

    public static EntityNode guess(Graph graph, String name, String toHash) {
        String baseId = ID.sanitize(name);
        Set<Edge> canMean = graph.edges(new String[]{"r/+can_mean", baseId, "*"});

        int maxDegree = -1;
        EntityNode entity = null;
        for (Edge e : canMean) {
            String id = e.getParticipantIds()[1];
            Vertex v = graph.get(id);
            if (v != null && v.type() == VertexType.Entity) {
                int degree = v.getDegree();
                if (degree > maxDegree) {
                    maxDegree = degree;
                    entity = (EntityNode)v;
                }
            }
        }

        if (entity == null) {
            entity = EntityNode.fromNsAndText(ID.hash(toHash), name);
        }

        return entity;
    }
}