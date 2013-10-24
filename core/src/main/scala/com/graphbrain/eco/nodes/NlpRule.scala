package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Context, Contexts, NodeType}

class NlpRule(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "nlp"

  override def ntype(ctxt: Context) = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context) = {
    // incomplete
    if (params(1).booleanValue(ctxts, ctxt)) {
      params(2).booleanValue(ctxts, ctxt)
    }
    else {
      false
    }
  }

  override protected def typeError() = error("the first part of a rule must be a boolean")
}