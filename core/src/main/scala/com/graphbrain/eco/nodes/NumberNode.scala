package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, Context, NodeType}

class NumberNode(val value: Double, lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  override def ntype = NodeType.Number
  override def numberValue(ctxts: Contexts, ctxt: Context) = value

  override def toString = value.toString

  override def equals(obj:Any) = obj match {
    case n: NumberNode => n.value == value
    case _ => false
  }
}