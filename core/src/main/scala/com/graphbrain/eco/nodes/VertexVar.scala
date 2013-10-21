package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class VertexVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.Vertex

  override def equals(obj:Any) = obj match {
    case v: VertexVar => v.name == name
    case _ => false
  }
}