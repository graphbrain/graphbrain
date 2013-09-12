package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class StringVar(prog: Prog, name: String, val value: String) extends VarNode(prog, name) {
  override val ntype = NodeType.String
  override def stringValue() = value
}