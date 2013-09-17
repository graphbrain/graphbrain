package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class NumberNode(val value: Double) extends ProgNode {
  override def ntype = NodeType.Number
  override def numberValue(ctxts: Contexts) = value

  override def toString = value.toString

  override def equals(obj:Any) = obj match {
    case n: NumberNode => n.value == value
    case _ => false
  }
}