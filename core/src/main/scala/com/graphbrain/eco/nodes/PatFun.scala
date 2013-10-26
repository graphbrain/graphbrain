package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Words, Context, Contexts, NodeType}

class PatFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "?"

  override def ntype(ctxt: Context): NodeType = NodeType.Boolean

  private def stepPointers(pointers: Array[Int], words: Int): Boolean = {
    val count = pointers.length

    if (pointers(0) < 0) {
      for (i <- 0 until count) pointers(i) = i
    }
    else {
      var curPointer = count - 1
      pointers(curPointer) += 1

      while (pointers(curPointer) >= words - count + curPointer + 1) {
        curPointer -= 1

        if (curPointer < 1) return false

        pointers(curPointer) += 1
        var pos = pointers(curPointer)
        for (i <- (curPointer + 1) until count) {
          pos += 1
          pointers(i) = pos
        }
      }
    }

    true
  }

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = {
    val words = ctxts.sentence.words.length
    val count = params.length

    if (count > words) return false

    val pointers = new Array[Int](count)
    pointers(0) = -1

    while(stepPointers(pointers, words)) {
      val newContext = new Context
      var matches = true
      for (i <- 0 until count) {
        val start = pointers(i)
        val end = if (i < (count - 1)) pointers(i + 1) else words

        val subStr = (for (j <- start until end) yield ctxts.sentence.words(j)).map(_.word).reduceLeft(_ + " " + _)
        val subPhrase = ctxts.sentence.words.slice(start, end)

        params(i) match {
          case v: VarNode => newContext.setWords(v.name, new Words(subPhrase))
          case s: StringNode => if (s.value != subStr) matches = false // should bail out
          case _ => // error
        }
      }

      if (matches) {
        ctxts.addContext(newContext)
      }
    }

    true
  }

  override protected def typeError() = error("parameters must be variables or strings")
}