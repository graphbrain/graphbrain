package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class BoolNode(val value: Boolean) extends ProgNode {
  override val ntype = NodeType.Boolean
  override def booleanValue() = value

  override def toString = value.toString

  override def equals(obj:Any) = obj match {
    case b: BoolNode => b.value == value
    case _ => false
  }
}