package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class BoolNode(val value: Boolean, lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  override def ntype = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetBoolean(this, value)

  override def toString = value.toString

  override def equals(obj:Any) = obj match {
    case b: BoolNode => b.value == value
    case _ => false
  }
}