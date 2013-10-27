package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType, Context}
import com.graphbrain.eco.NodeType.NodeType

class NotFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = "!"

  override def ntype(ctxt: Context): NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean =
    !params(0).booleanValue(ctxts, ctxt)
}