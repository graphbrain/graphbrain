package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class BooleanVarNode(prog: Prog, name: String, var state: Boolean) extends VarNode(prog, name) {
  def value = state

  def ntype = NodeType.Boolean
}