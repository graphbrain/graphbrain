package com.graphbrain.eco;

import com.graphbrain.db.*;

import java.util.Set;

public class EntityGuesser {

    public static EntityNode guess(Graph graph, String name, String toHash) {
        return EntityNode.fromNsAndText("", name);
    }
}
