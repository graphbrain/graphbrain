package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Words, Context, Contexts, NodeType}

class WWRule(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "ww"

  override def ntype(ctxt: Context) = NodeType.Words

  override def wordsValue(ctxts: Contexts, ctxt: Context): Words = {
    if (params(0).booleanValue(ctxts, ctxt))
      params(1).wordsValue(ctxts, ctxt)
    else
      Words.empty
  }

  override protected def typeError() = error("the first part of a rule must be a boolean")
}