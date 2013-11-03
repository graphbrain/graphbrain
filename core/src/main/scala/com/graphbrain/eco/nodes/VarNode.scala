package com.graphbrain.eco.nodes

import com.graphbrain.eco._
import com.graphbrain.eco.NodeType.NodeType

class VarNode(val name: String, lastTokenPos: Int) extends ProgNode(lastTokenPos) {

  var varType: NodeType = NodeType.Unknown

  override def ntype = varType

  override def booleanValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetBoolean(this, c.getBoolean(name))

  override def stringValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetString(this, c.getString(name))

  override def numberValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetNumber(this, c.getNumber(name))

  override def wordsValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetWords(this, c.getWords(name))

  override def vertexValue(ctxts: Contexts) =
    for (c <- ctxts.ctxts) c.setRetVertex(this, c.getVertex(name))

  def isGLobal = name(0) == '_'

  override def toString = "$" + name
}