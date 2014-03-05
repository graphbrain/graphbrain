package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

import java.util.List;

public class WVRecursion extends FunNode {

    public WVRecursion(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    @Override
    public String label() {return ":wv";}

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Vertex;}

    @Override
    public void eval(Contexts ctxts) {
        params[0].eval(ctxts);
        for (Context c : ctxts.getCtxts()) {
            List<Contexts> newCtxts = ctxts.getProg().wv(c.getRetWords(params[0]), ctxts.getDepth() + 1, c);

            for (Contexts nctxts : newCtxts) {
                for (Context nc : nctxts.getCtxts()) {
                    Context forkCtxt = c.copy();
                    forkCtxt.copyGlobalsFrom(nc);
                    forkCtxt.setRetVertex(this, nc.getTopRetVertex());
                    forkCtxt.addSubContext(nc);

                    //System.out.println(forkCtxt);

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