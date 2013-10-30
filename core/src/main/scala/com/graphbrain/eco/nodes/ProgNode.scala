package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Contexts}
import com.graphbrain.eco.NodeType.NodeType

abstract class ProgNode(val lastTokenPos: Int) {
  def ntype: NodeType

  def stringValue(ctxts: Contexts): Unit = {} // error
  def numberValue(ctxts: Contexts): Unit = {} // error
  def booleanValue(ctxts: Contexts): Unit = {} // error
  def wordsValue(ctxts: Contexts): Unit = {} // error
  def vertexValue(ctxts: Contexts): Unit = {} // error
  def verticesValue(ctxts: Contexts): Unit = {} // error

  def value(ctxts: Contexts) = ntype match {
    case NodeType.Boolean => booleanValue(ctxts)
    case NodeType.Number => numberValue(ctxts)
    case NodeType.Words => wordsValue(ctxts)
    case NodeType.String => stringValue(ctxts)
    case NodeType.Vertex => vertexValue(ctxts)
  }

  protected def error(msg: String) = println(msg)

  protected def typeError() = error("type error")
}