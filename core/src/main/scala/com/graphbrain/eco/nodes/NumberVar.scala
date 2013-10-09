package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class NumberVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.Number

  override def equals(obj:Any) = obj match {
    case n: NumberVar => n.name == name
    case _ => false
  }
}