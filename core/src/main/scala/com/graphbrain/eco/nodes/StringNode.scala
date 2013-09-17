package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class StringNode(val value: String) extends ProgNode {
  override def ntype = NodeType.String
  override def stringValue(ctxts: Contexts) = value

  override def toString = value

  override def equals(obj:Any) = obj match {
    case s: StringNode => s.value == value
    case _ => false
  }
}