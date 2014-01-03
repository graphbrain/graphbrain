package com.graphbrain.eco.nodes;

import com.graphbrain.eco.*;
import com.graphbrain.eco.NodeType;

public class WordsFun extends FunNode {

    public enum WordsFunType {
        EndsWith
    }

    private WordsFunType fun;

    public WordsFun(WordsFunType fun, ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
        this.fun = fun;
    }

    public WordsFun(WordsFunType fun, ProgNode[] params) {
        this(fun, params, -1);
    }

    @Override
    public String label() {
        switch(fun) {
            case EndsWith: return "ends-with";
        }
        return "?";
    }

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Boolean;}

    @Override
    public void eval(Contexts ctxts) {
        switch(fun) {
            case EndsWith:
                for (ProgNode p : params)
                    p.eval(ctxts);

                for (Context c : ctxts.getCtxts()) {
                    Words words1 = c.getRetWords(params[0]);
                    Words words2 = c.getRetWords(params[1]);

                    if (words1 == null) {
                        c.setRetBoolean(this, false);
                    }
                    else {
                        c.setRetBoolean(this, words1.endsWith(words2));
                    }
                }
            break;
        }
    }
}