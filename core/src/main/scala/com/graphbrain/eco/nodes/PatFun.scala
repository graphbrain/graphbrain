package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}
import com.graphbrain.eco.nodes.patterns.{VarPatternElem, StrPatternElem}

class PatFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "pat"

  override def ntype: NodeType = NodeType.Boolean

  val elems = params.map(f = {
    case s: StringNode => new StrPatternElem(s.value)
    case v: VarNode => new VarPatternElem(v.name, v.possiblePOS, v.necessaryPOS, v.forbiddenPOS)
    case _ => null // error
  })

  // init elements
  for (i <- 0 until elems.length) {
    val prev = if (i == 0) null else elems(i - 1)
    val next = if (i == elems.length - 1) null else elems(i + 1)
    elems(i).init(i, elems.length, prev, next)
  }

  println("\n+++++++++++++++")
  println(this)
  printMatch()

  override def booleanValue(ctxts: Contexts): Unit = {
    val words = ctxts.sentence.words.length
    val count = params.length

    if (count > words) return

    elems.foreach(_.resetSentence())

    matches(ctxts, 0)

    ctxts.applyChanges()
  }

  private def matches(ctxts: Contexts, pos: Int): Unit = {
    //println("+" + pos)

    elems(pos).fixed = true

    elems(pos).rewind()
    while (elems(pos).next(ctxts.sentence)) {
      if (pos == elems.length - 1) {
        // match found
        println("\n")
        println(ctxts.sentence)
        println(this)
        printMatch()
        addContext(ctxts)
      }
      else {
        //println(elems(pos))
        matches(ctxts, pos + 1)
      }
    }

    elems(pos).fixed = false
  }

  def addContext(ctxts: Contexts): Unit = {
    val newContext = new Context(ctxts)

    for (e <- elems) {
      e match {
        case v: VarPatternElem => newContext.setWords(v.name, v.curWords)
        case _ => // error
      }
    }

    ctxts.addContext(newContext)
    newContext.setRetBoolean(this, value = true)
  }

  def printMatch() =
    println(elems.map(_.toString).reduceLeft(_ + "\n" + _))
}