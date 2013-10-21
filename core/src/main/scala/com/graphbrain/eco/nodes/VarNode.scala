package com.graphbrain.eco.nodes

import com.graphbrain.eco.{PTree, Context, Contexts}
import scala.Predef.String
import scala.Boolean

class VarNode(val name: String, lastTokenPos: Int) extends ProgNode(lastTokenPos) {

  override def ntype(ctxt: Context) = ctxt.getType(name)

  override def stringValue(ctxts: Contexts, ctxt: Context): String =
    if (ctxt == null) "" else ctxt.getString(name)
  override def numberValue(ctxts: Contexts, ctxt: Context): Double =
    if (ctxt == null) 0 else ctxt.getNumber(name)
  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean =
    if (ctxt == null) false else ctxt.getBoolean(name)
  override def treeValue(ctxts: Contexts, ctxt: Context): PTree =
    if (ctxt == null) PTree.nullTree else ctxt.getTree(name)
  override def posValue(ctxts: Contexts, ctxt: Context): String =
    if (ctxt == null) "" else ctxt.getPOS(name)
  override def vertexValue(ctxts: Contexts, ctxt: Context): String =
    if (ctxt == null) "" else ctxt.getVertex(name)

  override def toString = "$" + name
}