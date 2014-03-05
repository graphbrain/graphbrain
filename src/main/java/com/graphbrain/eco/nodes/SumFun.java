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
    public NodeType ntype(Context ctxt) {
        return params[0].ntype(ctxt);
    }

    @Override
    public void eval(Contexts ctxts) {
        for (ProgNode p : params)
            p.eval(ctxts);

        for (Context c : ctxts.getCtxts()) {

            // determine sum type
            NodeType t = params[0].ntype(c);
            for (int i = 1; i < params.length; i++) {
                if (params[i].ntype(c) != t) {
                    t = NodeType.Unknown;
                    break;
                }
            }

            switch(t) {
                case Number:
                    double sum = 0;
                    for (ProgNode p : params)
                        sum += c.getRetNumber(p);
                    c.setRetNumber(this, sum);
                    break;
                case Words:
                    Words agg = Words.empty();
                    for (ProgNode p : params)
                        agg = agg.append(c.getRetWords(p));
                    c.setRetWords(this, agg);
                    break;
                default:
                    // error
                    break;
            }
        }
    }
}