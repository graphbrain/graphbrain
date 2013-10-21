package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Context, Contexts, NodeType}

class TreeRule(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "tree"

  override def ntype(ctxt: Context) = {
    params(0).ntype(ctxt) match {
      case NodeType.Boolean => NodeType.Unknown
      case _ => {
        typeError()
        NodeType.Unknown
      }
    }
  }

  override def booleanValue(ctxts: Contexts, ctxt: Context) = {
    // incomplete
    params(1).booleanValue(ctxts, ctxt)
  }

  override protected def typeError() = error("the first part of a rule must be a boolean")
}