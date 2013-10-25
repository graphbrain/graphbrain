package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType, Context}
import com.graphbrain.eco.NodeType.NodeType

class RecursionFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = ":"

  override def ntype(ctxt: Context): NodeType = NodeType.Vertex

  override def vertexValue(ctxts: Contexts, ctxt: Context): String = {
    val newCtxts = Contexts(ctxts.prog, params(0).wordsValue(ctxts, ctxt))
    ctxts.prog.verticesValue(newCtxts, null).head
  }
}
