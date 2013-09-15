package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class BoolVar(name: String, val value: Boolean) extends VarNode(name) {
  override val ntype = NodeType.Boolean
  override def booleanValue() = value

  override def equals(obj:Any) = obj match {
    case b: BoolVar => b.name == name
    case _ => false
  }
}