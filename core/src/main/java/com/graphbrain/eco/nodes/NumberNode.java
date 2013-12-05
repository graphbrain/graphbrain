package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class NumberNode extends ProgNode {
    private double value;

    public NumberNode(double value, int lastTokenPos) {
        super(lastTokenPos);
        this.value = value;
    }

    public NumberNode(double value) {
        this(value, -1);
    }

    @Override
    public NodeType ntype(){return NodeType.Number;}

    @Override
    public void numberValue(Contexts ctxts) {
        for (Context c : ctxts.getCtxts())
            c.setRetNumber(this, value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NumberNode) {
            NumberNode n = (NumberNode)obj;
            return n.value == value;
        }
        return false;
    }
}