package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

import java.util.List;

public class WWRecursion extends FunNode {

    public WWRecursion(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public WWRecursion(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label() {return ":ww";}

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Words;}

    @Override
    public void eval(Contexts ctxts) {
        params[0].eval(ctxts);
        for (Context c : ctxts.getCtxts()) {
            List<Contexts> newCtxts = ctxts.getProg().ww(c.getRetWords(params[0]), ctxts.getDepth() + 1, c);

            for (Contexts nctxts : newCtxts) {
                for (Context nc : nctxts.getCtxts()) {
                    Context forkCtxt = c.copy();
                    forkCtxt.setRetWords(this, nc.getTopRetWords());
                    forkCtxt.addSubContext(nc);

                    // add forked context to caller contexts
                    ctxts.addContext(forkCtxt);
                }
            }

            // remove original context
            ctxts.remContext(c);
        }
        ctxts.applyChanges();
    }
}
