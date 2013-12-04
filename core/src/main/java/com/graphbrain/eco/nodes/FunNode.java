package com.graphbrain.eco.nodes;

public abstract class FunNode extends ListNode {

    public FunNode(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public FunNode(ProgNode[] params) {
        this(params, -1);
    }

    public abstract String label();
}