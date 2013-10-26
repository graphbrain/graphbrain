package com.graphbrain.eco

import scala.collection.mutable
import com.graphbrain.nlp.Lemmatiser

class Contexts(val prog: Prog, val sentence: Words) {
  val ctxts = mutable.ListBuffer[Context]()

  private val addCtxts = mutable.ListBuffer[Context]()
  private val remCtxts = mutable.ListBuffer[Context]()

  def addContext(c: Context) = addCtxts += c
  def remContext(c: Context) = remCtxts += c

  def applyChanges() = {
    for (c <- addCtxts) ctxts += c
    for (c <- remCtxts) ctxts -= c
    addCtxts.clear()
    remCtxts.clear()
  }

  def print() = {
    println("Contexts: " + sentence)
    for (c <- ctxts) c.print()
  }
}

object Contexts {
  val l = new Lemmatiser

  def apply(prog: Prog, s: String) = new Contexts(prog, parseSentence(s))

  def apply(prog: Prog, w: Words) = new Contexts(prog, w)

  private def parseSentence(s: String) = {
    println(Contexts.l.annotate(s))
    new Words(Contexts.l.annotate(s).map(w => new Word(w._1, w._2, w._3)).toArray)
  }

  def main(args: Array[String]) = {

  }
}