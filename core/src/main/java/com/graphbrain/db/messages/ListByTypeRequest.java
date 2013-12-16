package com.graphbrain.db.messages;

import com.graphbrain.db.VertexType;

public class ListByTypeRequest {
    private VertexType vtype;

    public ListByTypeRequest() {
        vtype = null;
    }

    public ListByTypeRequest(VertexType vtype) {
        this.vtype = vtype;
    }

    public VertexType getVtype() {
        return vtype;
    }
}
