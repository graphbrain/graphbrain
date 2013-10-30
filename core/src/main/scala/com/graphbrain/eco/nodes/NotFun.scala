package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}
import com.graphbrain.eco.NodeType.NodeType

class NotFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = "!"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0).booleanValue(ctxts)

    for (c <- ctxts.ctxts)
      c.setRetBoolean(this, !c.getRetBoolean(params(0)))
  }
}