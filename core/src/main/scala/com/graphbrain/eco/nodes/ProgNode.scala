package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.Contexts

abstract class ProgNode {
  def ntype: NodeType

  def stringValue(ctxts: Contexts): String = "" // error
  def numberValue(ctxts: Contexts): Double = 0 // error
  def booleanValue(ctxts: Contexts): Boolean = false // error

  protected def error(msg: String) = {
    println(msg)
  }

  protected def typeError() = error("type error")
}