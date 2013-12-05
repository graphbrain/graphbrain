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
    public NodeType ntype() {return NodeType.Words;}

    @Override
    public void wordsValue(Contexts ctxts) {
        // eval pattern
        params[0].booleanValue(ctxts);
        // eval conds
        params[1].booleanValue(ctxts);
        // eval return value
        params[2].wordsValue(ctxts);

        for (Context c : ctxts.getCtxts())
            c.setRetWords(this, c.getRetWords(params[2]));
    }
}