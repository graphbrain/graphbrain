package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Words, Contexts, Context}

abstract class ProgNode(val lastTokenPos: Int) {
  def ntype(ctxt: Context): NodeType

  def stringValue(ctxts: Contexts, ctxt: Context): String = "" // error
  def numberValue(ctxts: Contexts, ctxt: Context): Double = 0 // error
  def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = false // error
  def wordsValue(ctxts: Contexts, ctxt: Context): Words = Words.empty // error
  def vertexValue(ctxts: Contexts, ctxt: Context): String = "" // error

  protected def error(msg: String) = println(msg)

  protected def typeError() = error("type error")
}