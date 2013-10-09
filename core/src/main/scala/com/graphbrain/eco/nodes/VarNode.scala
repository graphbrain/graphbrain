package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Word, Context, Contexts}

abstract class VarNode(val name: String, lastTokenPos: Int) extends ProgNode(lastTokenPos) {
  override def stringValue(ctxts: Contexts, ctxt: Context): String = ctxt.getString(name)
  override def numberValue(ctxts: Contexts, ctxt: Context): Double = ctxt.getNumber(name)
  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = ctxt.getBoolean(name)
  override def phraseValue(ctxts: Contexts, ctxt: Context): Array[Word] = ctxt.getPhrase(name)

  override def toString = name
}