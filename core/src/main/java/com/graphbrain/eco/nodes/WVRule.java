package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class WVRule extends RuleNode {

    public WVRule(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public WVRule(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public NodeType ntype() {return NodeType.Boolean;}

    @Override
    public void vertexValue(Contexts ctxts) {
        // eval pattern
        params[0].booleanValue(ctxts);
        // eval conditions
        params[1].booleanValue(ctxts);
        // eval return value
        params[2].vertexValue(ctxts);

        for (Context c : ctxts.getCtxts())
            c.setRetVertex(this, c.getRetVertex(params[2]));
  }
}