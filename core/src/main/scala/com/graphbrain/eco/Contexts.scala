package com.graphbrain.eco

import scala.collection.mutable
import com.graphbrain.eco.nodes.ProgNode

class Contexts(val rule: ProgNode, val prog: Prog, val sentence: Words) {
  var subContexts = List[Contexts]()

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

  def addSubContexts(subCtxtsList: List[Contexts]) =
    for (subCtxts <- subCtxtsList)
      subContexts :+= subCtxts

  override def toString = {
    val sb = new mutable.StringBuilder()
    sb.append("Contexts: " + sentence)
    for (c <- ctxts)
      sb.append(c)
    sb.toString()
  }
}

object Contexts {
  def apply(rule: ProgNode, prog: Prog, w: Words) = new Contexts(rule, prog, w)
}