package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Context, Contexts, NodeType}

class NlpRule(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "nlp"

  override def ntype(ctxt: Context) = NodeType.Boolean

  override def verticesValue(ctxts: Contexts, ctxt: Context): Set[String] = {
    if (params(1).booleanValue(ctxts, ctxt))
      params(2).verticesValue(ctxts, ctxt)
    else
      Set[String]()
  }

  override protected def typeError() = error("the first part of a rule must be a boolean")
}