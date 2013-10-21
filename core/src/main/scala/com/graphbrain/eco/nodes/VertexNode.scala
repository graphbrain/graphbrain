package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, Context, NodeType}

class VertexNode(val value: String, lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  override def ntype = NodeType.Vertex
  override def vertexValue(ctxts: Contexts, ctxt: Context) = value

  override def toString = value

  override def equals(obj:Any) = obj match {
    case v: VertexNode => v.value == value
    case _ => false
  }
}