package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class StringNode(prog: Prog, val value: String) extends ProgNode(prog) {
  override def eval(): Any = value

  def ntype = NodeType.String
}