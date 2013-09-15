package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class StringNode(val value: String) extends ProgNode {
  override val ntype = NodeType.String
  override def stringValue() = value

  override def toString = value

  override def equals(obj:Any) = obj match {
    case s: StringNode => s.value == value
    case _ => false
  }
}