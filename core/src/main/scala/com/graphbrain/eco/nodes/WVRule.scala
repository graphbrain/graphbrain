package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class WVRule(val name: String, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "wv"

  override def ntype = NodeType.Boolean

  override def vertexValue(ctxts: Contexts) = {
    params(0).booleanValue(ctxts)
    params(1).vertexValue(ctxts)

    for (c <- ctxts.ctxts)
      c.setRetVertex(this, c.getRetVertex(params(1)))
  }

  override protected def typeError() = error("the first part of a rule must be a boolean")
}