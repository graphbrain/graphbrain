package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class BoolNode(prog: Prog, val value: Boolean) extends ProgNode(prog) {
  override val ntype = NodeType.Boolean
  override def booleanValue() = value

  override def toString = value.toString
}