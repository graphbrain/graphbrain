package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType

abstract class ProgNode {
  val ntype: NodeType

  def stringValue(): String = "" // error
  def numberValue(): Double = 0 // error
  def booleanValue(): Boolean = false // error

  protected def error(msg: String) = {
    println(msg)
  }

  protected def typeError() = error("type error")
}