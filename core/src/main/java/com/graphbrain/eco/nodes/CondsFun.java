package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.NodeType;
import com.graphbrain.eco.Contexts;

public class CondsFun extends FunNode {

    public CondsFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public CondsFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return ";";}

    @Override
    public NodeType ntype(){return NodeType.Boolean;}

    @Override
    public void booleanValue(Contexts ctxts) {
        for (ProgNode p : params) {
            p.value(ctxts);
            if (p.ntype() == NodeType.Boolean) {
                for (Context c : ctxts.getCtxts()) {
                    if (!c.getRetBoolean(p)) {
                        ctxts.remContext(c);
                    }
                }
                ctxts.applyChanges();
            }
        }
    }
}