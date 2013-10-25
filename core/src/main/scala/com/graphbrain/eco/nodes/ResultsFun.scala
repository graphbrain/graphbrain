package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class ResultsFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "results"

  override def ntype(ctxt: Context): NodeType = {
    for (p <- params) {
      if (p.ntype(ctxt) != NodeType.Vertex) {
        typeError()
        return NodeType.Unknown
      }
    }
    NodeType.Boolean
  }

  override def verticesValue(ctxts: Contexts, ctxt: Context): Set[String] = {
    var vertices = Set[String]()

    for (p <- params)
      for (c <- ctxts.ctxts)
        vertices += p.vertexValue(ctxts, c)

    vertices
  }

  override protected def typeError() = error("parameters must be vertex")
}