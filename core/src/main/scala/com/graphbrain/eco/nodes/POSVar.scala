package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class POSVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.POS

  override def equals(obj:Any) = obj match {
    case p: POSVar => p.name == name
    case _ => false
  }
}