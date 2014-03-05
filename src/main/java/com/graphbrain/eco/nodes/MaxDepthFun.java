package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class MaxDepthFun extends FunNode {

    public MaxDepthFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public MaxDepthFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return "max-depth";}

    @Override
    public NodeType ntype(Context ctxt){return NodeType.Boolean;}

    @Override
    public void eval(Contexts ctxts) {
        params[0].eval(ctxts);

        for (Context c : ctxts.getCtxts()) {
            boolean r = ctxts.getDepth() <= c.getRetNumber(params[0]);
            c.setRetBoolean(this, r);
        }
    }
}