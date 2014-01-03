package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class WWRule extends RuleNode {

    public WWRule(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public WWRule(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label() {return "ww";}

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Words;}

    @Override
    public void eval(Contexts ctxts) {
        // eval pattern
        params[0].eval(ctxts);
        // eval conds
        params[1].eval(ctxts);
        // eval return value
        params[2].eval(ctxts);

        for (Context c : ctxts.getCtxts())
            c.setRetWords(this, c.getRetWords(params[2]));
    }
}