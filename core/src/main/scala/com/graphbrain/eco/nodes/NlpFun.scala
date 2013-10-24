package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType, Context}
import scala.Boolean
import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.nodes.NlpFunType.NlpFunType

object NlpFunType extends Enumeration {
  type NlpFunType = Value
  val POS, POSPRE, LEMMA = Value
}

class NlpFun(val funType: NlpFunType, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = funType match {
    case NlpFunType.POS => "pos"
    case NlpFunType.POSPRE => "pos-pre"
    case NlpFunType.LEMMA => "lemma"
  }

  override def ntype(ctxt: Context): NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = funType match {
    case NlpFunType.POS => {
      params(0) match {
        case v: VarNode => {
          val words = v.wordsValue(ctxts, ctxt)
          if (words.count != 1) return false
          words.words(0).pos == params(1).stringValue(ctxts, ctxt)
        }
      }
    }
    case NlpFunType.POSPRE => {
      params(0) match {
        case v: VarNode => {
          val words = v.wordsValue(ctxts, ctxt)
          if (words.count != 1) return false
          words.words(0).pos.startsWith(params(1).stringValue(ctxts, ctxt))
        }
      }
    }
    case NlpFunType.LEMMA => {
      params(0) match {
        case v: VarNode => {
          val words = v.wordsValue(ctxts, ctxt)
          if (words.count != 1) return false
          words.words(0).lemma == params(1).stringValue(ctxts, ctxt)
        }
      }
    }
  }
}