package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

class WordsFun(val fun: WordsFun.WordsFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = fun match {
    case WordsFun.EndsWith => "ends-with"
  }

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    fun match {
      case WordsFun.EndsWith => {
        for (p <- params)
          p.wordsValue(ctxts)

        for (c <- ctxts.ctxts) {
          val words1 = c.getRetWords(params(0))
          val words2 = c.getRetWords(params(1))

          c.setRetBoolean(this, words1.endsWith(words2))
        }
      }
    }
  }
}

object WordsFun extends Enumeration {
  type WordsFun = Value
  val EndsWith = Value
}