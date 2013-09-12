package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class NumberVar(prog: Prog, name: String, var value: Double) extends VarNode(prog, name) {
  override val ntype = NodeType.Number
  override def numberValue() = value
}