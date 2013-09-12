package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class BoolVar(prog: Prog, name: String, val value: Boolean) extends VarNode(prog, name) {
  override val ntype = NodeType.Boolean
  override def booleanValue() = value
}