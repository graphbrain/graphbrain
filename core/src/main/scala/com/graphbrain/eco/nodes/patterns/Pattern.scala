package com.graphbrain.eco.nodes.patterns

import com.graphbrain.eco.Words

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
  def main(args: Array[String]) = {

    val sentence = Words.fromString("Telmo likes eating chocolate.")
    println(sentence)

    val a = new VarPatternElem("a")
    val v = new VarPatternElem("v", Array("V"))
    //val s = new StrPatternElem("likes")
    val c = new VarPatternElem("c")

    val pattern = new Pattern(Array(a, v, c))
    pattern.matches(sentence)
  }
}