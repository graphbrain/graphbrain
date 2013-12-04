package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public abstract class ProgNode {

    private int lastTokenPos;

    public ProgNode(int lastTokenPos) {
        this.lastTokenPos = lastTokenPos;
    }

    public abstract NodeType ntype();

    public void stringValue(Contexts ctxts) {} // error
    public void numberValue(Contexts ctxts) {} // error
    public void booleanValue(Contexts ctxts) {} // error
    public void wordsValue(Contexts ctxts) {} // error
    public void vertexValue(Contexts ctxts) {} // error
    public void verticesValue(Contexts ctxts) {} // error

    public void value(Contexts ctxts) {
        switch(ntype()) {
            case Boolean:
                booleanValue(ctxts);
                break;
            case Number:
                numberValue(ctxts);
                break;
            case Words:
                wordsValue(ctxts);
                break;
            case String:
                stringValue(ctxts);
                break;
            case Vertex:
                vertexValue(ctxts);
                break;
        }
    }

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