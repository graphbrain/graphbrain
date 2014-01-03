package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public abstract class ProgNode {

    private int lastTokenPos;

    public ProgNode(int lastTokenPos) {
        this.lastTokenPos = lastTokenPos;
    }

    public abstract NodeType ntype(Context ctxt);

    public abstract void eval(Contexts ctxts);

    /*
    protected void stringValue(Context ctxt) {} // error
    protected void numberValue(Context ctxt) {} // error
    protected void booleanValue(Context ctxt) {} // error
    protected void wordsValue(Context ctxt) {} // error
    protected void vertexValue(Context ctxt) {} // error

    public void value(Contexts ctxts) {
        for (Context c : ctxts.getCtxts()) {
            switch(ntype(c)) {
                case Boolean:
                    booleanValue(c);
                    break;
                case Number:
                    numberValue(c);
                    break;
                case Words:
                    wordsValue(c);
                    break;
                case String:
                    stringValue(c);
                    break;
                case Vertex:
                    vertexValue(c);
                    break;
                case Unknown:
                    // error
                break;
            }
        }
    }
    */

    protected void error(String msg) {
        System.out.println(msg);
    }

    protected void typeError() {
        error("type error");
    }

    public int getLastTokenPos() {
        return lastTokenPos;
    }
}