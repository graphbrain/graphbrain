package com.graphbrain.eco.nodes;

import com.graphbrain.eco.NodeType;

public class NotFun extends FunNode {

    public NotFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
    }

    public NotFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label(){return  "!";}

    @override
    def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0).booleanValue(ctxts)

    for (c <- ctxts.ctxts)
      c.setRetBoolean(this, !c.getRetBoolean(params(0)))
  }
}