package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Words, Contexts}

class SumFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "+"

  override def ntype: NodeType = params(0).ntype

  override def numberValue(ctxts: Contexts) = {
    for (p <- params)
      p.numberValue(ctxts)

    for (c <- ctxts.ctxts) {
      var sum: Double = 0
      for (p <- params)
        sum += c.getRetNumber(p)
      c.setRetNumber(this, sum)
    }
  }

  override def wordsValue(ctxts: Contexts) = {
    for (p <- params)
      p.wordsValue(ctxts)

    for (c <- ctxts.ctxts) {
      var agg = Words.empty
      for (p <- params)
        agg += c.getRetWords(p)
      c.setRetWords(this, agg)
    }
  }
}