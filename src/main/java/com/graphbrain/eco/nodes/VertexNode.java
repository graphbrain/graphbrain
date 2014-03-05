package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;
import com.graphbrain.db.Vertex;

public class VertexNode extends ProgNode {

    private Vertex value;

    public VertexNode(Vertex value, int lastTokenPos) {
        super(lastTokenPos);
        this.value = value;
    }

    public VertexNode(Vertex value) {
        this(value, -1);
    }

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Vertex;}

    @Override
    public void eval(Contexts ctxts) {
        for (Context c : ctxts.getCtxts())
            c.setRetVertex(this, value);
    }

    @Override
    public String toString() {return value.toString();}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VertexNode) {
            VertexNode v = (VertexNode)obj;
            return v.value == value;
        }
        return false;
    }
}