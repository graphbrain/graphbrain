package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class StringVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.String

  override def equals(obj:Any) = obj match {
    case s: StringVar => s.name == name
    case _ => false
  }
}