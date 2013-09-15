package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class StringNode(prog: Prog, val value: String) extends ProgNode(prog) {
  override val ntype = NodeType.String
  override def stringValue() = value

  override def toString = value
}