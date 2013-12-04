package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}
import com.graphbrain.db.Vertex

class VertexNode(val value: Vertex, lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  override def ntype = NodeType.Vertex

  override def vertexValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetVertex(this, value)

  override def toString = value.toString

  override def equals(obj:Any) = obj match {
    case v: VertexNode => v.value == value
    case _ => false
  }
}