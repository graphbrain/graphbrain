package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class BoolNode extends ProgNode {

    private boolean value;

    public BoolNode(boolean value, int lastTokenPos) {
        super(lastTokenPos);
        this.value = value;
    }

    public BoolNode(boolean value) {
        this(value, -1);
    }

    @Override
    public NodeType ntype() {
        return NodeType.Boolean;
    }

    @Override
    public void booleanValue(Contexts ctxts) {
        for (Context c : ctxts.getCtxts())
            c.setRetBoolean(this, value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoolNode) {
            BoolNode b = (BoolNode)obj;
            return b.value == value;
        }
        else {
            return false;
        }
    }
}