package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class DummyFun extends FunNode {

    private String name;

    public DummyFun(String name, ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
        this.name = name;
    }

    public DummyFun(String name, ProgNode[] params) {
        this(name, params, -1);
    }

    @Override
    public String label(){return name;}

    @Override
    public NodeType ntype(Context ctxt) {
        return NodeType.Boolean;
    }

    @Override
    public void eval(Contexts ctxts) {

    }

    public String getName() {
        return name;
    }
}