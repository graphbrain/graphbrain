package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog
import com.graphbrain.eco.NodeType.NodeType

abstract class ProgNode(val prog: Prog) {
  val ntype: NodeType

  def stringValue(): String = "" // error
  def numberValue(): Double = 0 // error
  def booleanValue(): Boolean = false // error
}