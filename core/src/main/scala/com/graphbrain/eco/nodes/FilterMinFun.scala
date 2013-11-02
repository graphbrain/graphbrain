package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class FilterMinFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "filter-min"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0).stringValue(ctxts)

    var bestCtxt: Context = null
    var minVal = Double.PositiveInfinity

    for (c <- ctxts.ctxts) {
      val value = c.getNumber(c.getRetString(params(0)))
      if (value < minVal) {
        bestCtxt = c
        minVal = value
      }
    }

    for (c <- ctxts.ctxts)
      c.setRetBoolean(this, c eq bestCtxt)
  }

  override protected def typeError() = error("parameters must be boolean")
}