package com.graphbrain.db;

import com.esotericsoftware.kryo.Kryo;
import com.graphbrain.db.messages.*;

public class Network {
    public static void registerMessages(Kryo kryo) {
        kryo.register(OK.class);
        kryo.register(GetRequest.class);
        kryo.register(GetResponse.class);
        kryo.register(PutRequest.class);
        kryo.register(UpdateRequest.class);
        kryo.register(RemoveRequest.class);
        kryo.register(AssociateEmailToUsernameRequest.class);
        kryo.register(UsernameByEmailRequest.class);
        kryo.register(UsernameByEmailResponse.class);
        kryo.register(ListByTypeRequest.class);
        kryo.register(ListByTypeResponse.class);
        kryo.register(AltsRequest.class);
        kryo.register(AltsResponse.class);
        kryo.register(EdgesRequest.class);
        kryo.register(EdgesResponse.class);
        kryo.register(AddLinkToGlobalRequest.class);
        kryo.register(RemoveLinkToGlobalRequest.class);
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
