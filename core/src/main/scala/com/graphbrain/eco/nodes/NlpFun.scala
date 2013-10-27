package com.graphbrain.eco.nodes

import com.graphbrain.eco._
import scala.Boolean
import com.graphbrain.eco.nodes.NlpFunType.NlpFunType
import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.NodeType

object NlpFunType extends Enumeration {
  type NlpFunType = Value
  val IS_POS,
    IS_POSPRE,
    ARE_POS,
    ARE_POSPRE,
    CONTAINS_POS,
    CONTAINS_POSPRE,
    IS_LEMMA = Value
}

class NlpFun(val funType: NlpFunType, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = funType match {
    case NlpFunType.IS_POS => "is-pos"
    case NlpFunType.IS_POSPRE => "is-pos-pre"
    case NlpFunType.ARE_POS => "are-pos"
    case NlpFunType.ARE_POSPRE => "are-pos-pre"
    case NlpFunType.CONTAINS_POS => "contains-pos"
    case NlpFunType.CONTAINS_POSPRE => "contains-pos-pre"
    case NlpFunType.IS_LEMMA => "is-lemma"
  }

  override def ntype(ctxt: Context): NodeType = NodeType.Boolean

  private def matchPoses(word: Word, pre: Boolean, poses: Array[String]): Boolean = {
    for (pos <- poses)
      if (pre)
        if (word.pos.startsWith(pos)) return true
      else
        if (word.pos == pos) return true

    false
  }

  private def matchPoses(words: Words, pre: Boolean, ctxts: Contexts, ctxt: Context): Boolean = {
    val poses = params.drop(1).map(_.stringValue(ctxts, ctxt))

    for (word <- words.words)
      if (!matchPoses(word, pre, poses)) return false

    true
  }

  private def containsPoses(words: Words, pre: Boolean, ctxts: Contexts, ctxt: Context): Boolean = {
    val poses = params.drop(1).map(_.stringValue(ctxts, ctxt))

    for (word <- words.words)
      if (matchPoses(word, pre, poses)) return true

    false
  }

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = {
    val words = params(0).wordsValue(ctxts, ctxt)

    funType match {
      case NlpFunType.IS_POS => {
        if (words.count != 1) return false
        matchPoses(words, pre = false, ctxts, ctxt)
      }
      case NlpFunType.IS_POSPRE => {
        if (words.count != 1) return false
        matchPoses(words, pre = true, ctxts, ctxt)
      }
      case NlpFunType.ARE_POS => {
        if (words.count == 0) return false
        matchPoses(words, pre = false, ctxts, ctxt)
      }
      case NlpFunType.ARE_POSPRE => {
        if (words.count == 0) return false
        matchPoses(words, pre = true, ctxts, ctxt)
      }
      case NlpFunType.CONTAINS_POS => {
        if (words.count == 0) return false
        containsPoses(words, pre = false, ctxts, ctxt)
      }
      case NlpFunType.CONTAINS_POSPRE => {
        if (words.count == 0) return false
        containsPoses(words, pre = true, ctxts, ctxt)
      }
      case NlpFunType.IS_LEMMA => {
        if (words.count != 1) return false
        words.words(0).lemma == params(1).stringValue(ctxts, ctxt)
      }
    }
  }
}