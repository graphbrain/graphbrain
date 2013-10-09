package com.graphbrain.eco

import scala.collection.mutable
import com.graphbrain.nlp.Lemmatiser

class Contexts(s: String) {
  val ctxts = mutable.ListBuffer[Context]()
  val sentence = parseSentence(s).toArray

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

  private def parseSentence(s: String) =
    Contexts.l.annotate(s).map(w => new Word(w._1, w._2, w._3))

  def print() = {
    for (c <- ctxts) c.print()
  }
}

object Contexts {
  val l = new Lemmatiser
}