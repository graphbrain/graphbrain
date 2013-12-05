package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.NodeType;
import com.graphbrain.eco.Contexts;

public class PosFun extends FunNode {

    public PosFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public PosFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return "pos";}

    @Override
    public NodeType ntype(){return NodeType.Number;}

    @Override
    public void numberValue(Contexts ctxts) {
        ProgNode p = params[0];
        p.wordsValue(ctxts);
        for (Context c : ctxts.getCtxts()) {
            double pos = c.getRetWords(p).getPos();
            c.setRetNumber(this, pos);
        }
    }
}