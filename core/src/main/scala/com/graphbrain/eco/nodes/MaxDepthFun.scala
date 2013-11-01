package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

class MaxDepthFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "max-depth"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0).numberValue(ctxts)

    for (c <- ctxts.ctxts) {
      val r = ctxts.depth <= c.getRetNumber(params(0))
      c.setRetBoolean(this, r)
    }
  }

  override protected def typeError() = error("parameters must be boolean")
}