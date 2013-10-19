package com.graphbrain.eco.nodes

import com.graphbrain.eco.{PTree, Context, Contexts}

abstract class VarNode(val name: String, lastTokenPos: Int) extends ProgNode(lastTokenPos) {
  override def stringValue(ctxts: Contexts, ctxt: Context): String =
    if (ctxt == null) "" else ctxt.getString(name)
  override def numberValue(ctxts: Contexts, ctxt: Context): Double =
    if (ctxt == null) 0 else ctxt.getNumber(name)
  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean =
    if (ctxt == null) false else ctxt.getBoolean(name)
  override def treeValue(ctxts: Contexts, ctxt: Context): PTree =
    if (ctxt == null) PTree.nullTree else ctxt.getPhrase(name)

  override def toString = name
}