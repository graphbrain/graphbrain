package com.graphbrain.db;

import com.esotericsoftware.kryo.Kryo;
import com.graphbrain.db.messages.GetResponse;
import com.graphbrain.db.messages.GetRequest;

public class Messages {
    public static void register(Kryo kryo) {
        kryo.register(GetRequest.class);
        kryo.register(GetResponse.class);
        kryo.register(Vertex.class);
        kryo.register(VertexType.class);
        kryo.register(EntityNode.class);
        kryo.register(Edge.class);
        kryo.register(EdgeType.class);
        kryo.register(ProgNode.class);
        kryo.register(TextNode.class);
        kryo.register(URLNode.class);
    }
}
