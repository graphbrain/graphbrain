package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class NotFun extends FunNode {

    public NotFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public NotFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return  "!";}

    @Override
    public NodeType ntype(){return NodeType.Boolean;}

    @Override
    public void booleanValue(Contexts ctxts) {
        params[0].booleanValue(ctxts);

        for (Context c : ctxts.getCtxts())
            c.setRetBoolean(this, !c.getRetBoolean(params[0]));
    }
}