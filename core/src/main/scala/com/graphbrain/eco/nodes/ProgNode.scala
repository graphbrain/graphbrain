package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{PTree, Contexts, Context}

abstract class ProgNode(val lastTokenPos: Int) {
  def ntype: NodeType

  def stringValue(ctxts: Contexts, ctxt: Context): String = "" // error
  def numberValue(ctxts: Contexts, ctxt: Context): Double = 0 // error
  def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = false // error
  def treeValue(ctxts: Contexts, ctxt: Context): PTree = null // error

  protected def error(msg: String) = println(msg)

  protected def typeError() = error("type error")
}