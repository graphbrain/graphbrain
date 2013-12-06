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
    public NodeType ntype() {return NodeType.Vertex;}

    @Override
    public void vertexValue(Contexts ctxts) {
        params[0].wordsValue(ctxts);
        for (Context c : ctxts.getCtxts()) {
            List<Contexts> newCtxts = ctxts.getProg().wv(c.getRetWords(params[0]), ctxts.getDepth() + 1);

            for (Contexts nctxts : newCtxts) {
                for (Context nc : nctxts.getCtxts()) {
                    Context forkCtxt = c.copy();
                    forkCtxt.setRetVertex(this, nc.getTopRetVertex());
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