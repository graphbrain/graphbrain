package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class LetFun extends FunNode {

    public LetFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public LetFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return "let";}

    @Override
    public NodeType ntype(Context ctxt){return NodeType.Boolean;}

    @Override
    public void eval(Contexts ctxts) {
        // TODO: check if params[0] is a var
        VarNode v = (VarNode)params[0];
        ProgNode p = params[1];

        p.eval(ctxts);

        for (Context c : ctxts.getCtxts()) {
            switch(p.ntype(c)) {
                case Boolean:
                    c.setBoolean(v.getName(), c.getRetBoolean(p));
                    break;
                case Number:
                    c.setNumber(v.getName(), c.getRetNumber(p));
                    break;
                case Words:
                    c.setWords(v.getName(), c.getRetWords(p));
                    break;
                case String:
                    c.setString(v.getName(), c.getRetString(p));
                    break;
                case Vertex:
                    c.setVertex(v.getName(), c.getRetVertex(p));
                    break;
            }

            c.setRetBoolean(this, true);
        }
    }
}