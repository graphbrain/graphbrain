package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class BoolNode(prog: Prog, val value: Boolean) extends ProgNode(prog) {
  override def eval(): AnyVal = value

  def ntype = NodeType.Boolean
}