package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.NodeType;
import com.graphbrain.eco.Words;
import com.graphbrain.eco.Contexts;

public class SumFun extends FunNode {

    public SumFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public SumFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return "+";}

    @Override
    public NodeType ntype() {
        return params[0].ntype();
    }

    @Override
    public void numberValue(Contexts ctxts) {
        for (ProgNode p : params) {
            p.numberValue(ctxts);
        }

        for (Context c : ctxts.getCtxts()) {
            double sum = 0;
            for (ProgNode p : params)
                sum += c.getRetNumber(p);
            c.setRetNumber(this, sum);
        }
    }

    @Override
    public void wordsValue(Contexts ctxts) {
        for (ProgNode p : params)
            p.wordsValue(ctxts);

        for (Context c : ctxts.getCtxts()) {
            Words agg = Words.empty();
            for (ProgNode p : params)
                agg.append(c.getRetWords(p));
            c.setRetWords(this, agg);
        }
    }
}