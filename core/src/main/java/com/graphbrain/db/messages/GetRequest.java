package com.graphbrain.db.messages;

import com.graphbrain.db.VertexType;

public class GetRequest {
    private String id;
    private VertexType vtype;

    public GetRequest() {
        id = "";
        vtype = null;
    }

    public GetRequest(String id, VertexType vtype) {
        this.id = id;
        this.vtype = vtype;
    }

    public String getId() {
        return id;
    }

    public VertexType getVtype() {
        return vtype;
    }
}
