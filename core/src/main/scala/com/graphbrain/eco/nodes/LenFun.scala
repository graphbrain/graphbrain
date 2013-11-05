package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

class LenFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "len"

  override def ntype: NodeType = NodeType.Number

  override def numberValue(ctxts: Contexts) = {
    val p = params(0)
    p.wordsValue(ctxts)
    for (c <- ctxts.ctxts) {
      val len = c.getRetWords(p).count
      c.setRetNumber(this, len)
    }
  }
}