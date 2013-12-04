package com.graphbrain.eco.nodes

import com.graphbrain.eco._
import com.graphbrain.eco.NodeType.NodeType

class VarNode(val name: String,
              val possiblePOS: Array[String],
              val necessaryPOS: Array[String],
              val forbiddenPOS: Array[String],
              lastTokenPos: Int) extends ProgNode(lastTokenPos) {

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

  override def toString = name
}

object VarNode {
  def apply(varStr: String, lastTokenPos: Int) = {
    val parts = varStr.split(":")
    val name = parts(0)

    val constStrs = if (parts.length > 1)
      parts(1).split("\\|")
    else
      Array[String]()

    val possiblePOS = constStrs
      .filter(_.charAt(0) != '-')
      .map(p => if (p.charAt(0) == '+') p.substring(1) else p)

    val necessaryPOS = constStrs
      .filter(_.charAt(0) == '+')
      .map(_.substring(1))

    val forbiddenPOS = constStrs
      .filter(_.charAt(0) == '+')
      .map(_.substring(1))

    new VarNode(name, possiblePOS, necessaryPOS, forbiddenPOS, lastTokenPos)
  }
}