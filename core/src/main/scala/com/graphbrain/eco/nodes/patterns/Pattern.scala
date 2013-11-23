package com.graphbrain.eco.nodes.patterns

import com.graphbrain.eco.Words
import com.graphbrain.eco.nodes.{VarNode, StringNode, PatFun}

class Pattern(val elems: Array[PatternElem]) {

  // init elements
  for (i <- 0 until elems.length) {
    val prev = if (i == 0) null else elems(i - 1)
    val next = if (i == elems.length - 1) null else elems(i + 1)
    elems(i).init(i, elems.length, prev, next)
  }

  def matches(sentence: Words) = {
    elems.foreach(_.resetSentence())

    matchSentence(sentence, 0)
  }

  private def matchSentence(sentence: Words, pos: Int): Unit = {
    //println("+" + pos)

    elems(pos).fixed = true

    elems(pos).rewind()
    while (elems(pos).next(sentence)) {
      if (pos == elems.length - 1) {
        // match found
        println("\n" + this)
      }
      else {
        //println(elems(pos))
        matchSentence(sentence, pos + 1)
      }
    }

    elems(pos).fixed = false
  }

  override def toString = elems.map(_.toString).reduceLeft(_ + "\n" + _)
}

object Pattern {
  def apply(patFun: PatFun) = {
    val elems = patFun.params.map(f = {
      case s: StringNode => new StrPatternElem(s.value)
      case v: VarNode => new VarPatternElem(v.name, v.possiblePOS, v.necessaryPOS, v.forbiddenPOS)
      case _ => null // error
    })

    new Pattern(elems)
  }

  def main(args: Array[String]) = {

    val sentence = Words.fromString("The capital city of Germany")
    println(sentence)

    val a = new VarPatternElem("a")
    //val v = new VarPatternElem("v")//, Array("V"))
    val the = new StrPatternElem("the")
    val of = new StrPatternElem("of")
    val b = new VarPatternElem("b")

    val pattern = new Pattern(Array(a, of, b))
    pattern.matches(sentence)
  }
}