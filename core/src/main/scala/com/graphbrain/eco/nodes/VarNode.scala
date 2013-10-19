package com.graphbrain.eco.nodes

import com.graphbrain.eco.{PTree, Context, Contexts}

abstract class VarNode(val name: String, lastTokenPos: Int) extends ProgNode(lastTokenPos) {
  override def stringValue(ctxts: Contexts, ctxt: Context): String = ctxt.getString(name)
  override def numberValue(ctxts: Contexts, ctxt: Context): Double = ctxt.getNumber(name)
  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = ctxt.getBoolean(name)
  override def treeValue(ctxts: Contexts, ctxt: Context): PTree = ctxt.getPhrase(name)

  override def toString = name
}