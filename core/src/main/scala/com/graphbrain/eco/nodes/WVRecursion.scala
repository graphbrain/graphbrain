package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType, Context}
import com.graphbrain.eco.NodeType.NodeType

class WVRecursion(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = ":wv"

  override def ntype(ctxt: Context): NodeType = NodeType.Vertex

  override def vertexValue(ctxts: Contexts, ctxt: Context): String = {
    val vertices = ctxts.prog.wv(params(0).wordsValue(ctxts, ctxt)).filter(_ != "")
    if (vertices.size > 0) vertices.head else ""
  }
}
