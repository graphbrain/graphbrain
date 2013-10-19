package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class TreeVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.PTree

  override def equals(obj:Any) = obj match {
    case p: TreeVar => p.name == name
    case _ => false
  }
}