package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class BoolVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.Boolean

  override def equals(obj:Any) = obj match {
    case b: BoolVar => b.name == name
    case _ => false
  }
}