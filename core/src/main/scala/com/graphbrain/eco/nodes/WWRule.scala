package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class WWRule(name: String, params: Array[ProgNode], lastTokenPos: Int= -1)
  extends RuleNode(name, params, lastTokenPos) {

  override val label = "ww"

  override def ntype = NodeType.Words

  override def wordsValue(ctxts: Contexts) = {
    params(0).booleanValue(ctxts)
    params(1).wordsValue(ctxts)

    for (c <- ctxts.ctxts)
      c.setRetWords(this, c.getRetWords(params(1)))
  }

  override protected def typeError() = error("the first part of a rule must be a boolean")
}