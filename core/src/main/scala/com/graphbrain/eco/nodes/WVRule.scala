package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class WVRule(params: Array[ProgNode], lastTokenPos: Int= -1)
  extends RuleNode(params, lastTokenPos) {

  override val label = "wv"

  override def ntype = NodeType.Boolean

  override def vertexValue(ctxts: Contexts) = {
    // eval pattern
    params(0).booleanValue(ctxts)
    // eval conditions
    params(1).booleanValue(ctxts)
    // eval return value
    params(2).vertexValue(ctxts)

    for (c <- ctxts.ctxts)
      c.setRetVertex(this, c.getRetVertex(params(2)))
  }
}