package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.Contexts

abstract class ProgNode(val lastTokenPos: Int) {
  def ntype: NodeType

  def stringValue(ctxts: Contexts): Unit = {} // error
  def numberValue(ctxts: Contexts): Unit = {} // error
  def booleanValue(ctxts: Contexts): Unit = {} // error
  def wordsValue(ctxts: Contexts): Unit = {} // error
  def vertexValue(ctxts: Contexts): Unit = {} // error
  def verticesValue(ctxts: Contexts): Unit = {} // error

  protected def error(msg: String) = println(msg)

  protected def typeError() = error("type error")
}