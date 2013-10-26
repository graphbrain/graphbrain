package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Words, Contexts, NodeType, Context}
import com.graphbrain.eco.NodeType.NodeType

class WWRecursion(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = ":ww"

  override def ntype(ctxt: Context): NodeType = NodeType.Words

  override def wordsValue(ctxts: Contexts, ctxt: Context): Words =
    ctxts.prog.ww(params(0).wordsValue(ctxts, ctxt))
}
