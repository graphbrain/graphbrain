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

  override def ntype: NodeType = NodeType.Boolean

  private def matchPoses(word: Word, pre: Boolean, poses: Array[String]): Boolean = {
    for (pos <- poses)
      if (pre)
        if (word.pos.startsWith(pos)) return true
      else
        if (word.pos == pos) return true

    false
  }

  private def matchPoses(words: Words, pre: Boolean, ctxt: Context): Boolean = {
    val poses = params.drop(1).map(ctxt.getRetString)

    for (word <- words.words)
      if (!matchPoses(word, pre, poses)) return false

    true
  }

  private def containsPoses(words: Words, pre: Boolean, ctxt: Context): Boolean = {
    val poses = params.drop(1).map(ctxt.getRetString)

    for (word <- words.words)
      if (matchPoses(word, pre, poses)) return true

    false
  }

  override def booleanValue(ctxts: Contexts) = {
    params(0).wordsValue(ctxts)
    params.drop(1).map(_.stringValue(ctxts))

    for (c <- ctxts.ctxts) {
      val words = c.getRetWords(params(0))

      funType match {
        case NlpFunType.IS_POS => {
          c.setRetBoolean(this, if (words.length != 1)
            false
          else
            matchPoses(words, pre = false, c))
        }
        case NlpFunType.IS_POSPRE => {
          c.setRetBoolean(this, if (words.length != 1)
            false
          else
            matchPoses(words, pre = true, c))
        }
        case NlpFunType.ARE_POS => {
          c.setRetBoolean(this, if (words.length == 0)
            false
          else
            matchPoses(words, pre = false, c))
        }
        case NlpFunType.ARE_POSPRE => {
          c.setRetBoolean(this, if (words.length == 0)
            false
          else
            matchPoses(words, pre = true, c))
        }
        case NlpFunType.CONTAINS_POS => {
          c.setRetBoolean(this, if (words.length == 0)
            false
          else
            containsPoses(words, pre = false, c))
        }
        case NlpFunType.CONTAINS_POSPRE => {
          c.setRetBoolean(this, if (words.length == 0)
            false
          else
            containsPoses(words, pre = true, c))
        }
        case NlpFunType.IS_LEMMA => {
          c.setRetBoolean(this, if (words.length != 1)
            false
          else
            words.words(0).lemma == c.getRetString(params(1)))
        }
      }
    }
  }
}