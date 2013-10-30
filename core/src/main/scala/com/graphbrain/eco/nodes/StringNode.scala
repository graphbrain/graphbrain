package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class StringNode(val value: String, lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  override def ntype = NodeType.String

  override def stringValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetString(this, value)

  override def toString = "\"" + value + "\""

  override def equals(obj:Any) = obj match {
    case s: StringNode => s.value == value
    case _ => false
  }
}