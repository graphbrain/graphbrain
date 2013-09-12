package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class NumberVarNode(prog: Prog, name: String, var state: Double) extends VarNode(prog, name) {
  def value = state

  def ntype = NodeType.Number
}