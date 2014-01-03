package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class StringNode extends ProgNode {

    private String value;

    public StringNode(String value, int lastTokenPos) {
        super(lastTokenPos);
        this.value = value;
    }

    public StringNode(String value) {
        this(value, -1);
    }

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.String;}

    @Override
    public void eval(Contexts ctxts) {
        for (Context c : ctxts.getCtxts())
            c.setRetString(this, value);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringNode) {
            StringNode s = (StringNode)obj;
            return s.value.equals(value);
        }
        return false;
    }

    public String getValue() {
        return value;
    }
}