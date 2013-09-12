package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class StringVarNode(prog: Prog, name: String, var state: String) extends VarNode(prog, name) {
  def value = state

  def ntype = NodeType.String
}