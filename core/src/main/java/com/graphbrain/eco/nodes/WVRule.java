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
    public String label() {return "wv";}

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Boolean;}

    @Override
    public void eval(Contexts ctxts) {
        // eval pattern
        params[0].eval(ctxts);
        // eval conditions
        params[1].eval(ctxts);
        // eval return value
        params[2].eval(ctxts);

        for (Context c : ctxts.getCtxts())
            c.setRetVertex(this, c.getRetVertex(params[2]));
    }
}