package com.graphbrain.eco

import scala.collection.mutable

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
  def apply(prog: Prog, w: Words) = new Contexts(prog, w)
}