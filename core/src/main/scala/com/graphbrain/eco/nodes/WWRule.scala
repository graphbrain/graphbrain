package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class WWRule(params: Array[ProgNode], lastTokenPos: Int= -1)
  extends RuleNode(params, lastTokenPos) {

  override val label = "ww"

  override def ntype = NodeType.Words

  override def wordsValue(ctxts: Contexts) = {
    // eval pattern
    params(0).booleanValue(ctxts)
    // eval conds
    params(1).booleanValue(ctxts)
    // eval return value
    params(2).wordsValue(ctxts)

    for (c <- ctxts.ctxts)
      c.setRetWords(this, c.getRetWords(params(2)))
  }
}