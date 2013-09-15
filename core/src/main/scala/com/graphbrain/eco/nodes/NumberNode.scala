package com.graphbrain.eco.nodes

import com.graphbrain.eco.{NodeType, Prog}

class NumberNode(prog: Prog, val value: Double) extends ProgNode(prog) {
  override val ntype = NodeType.Number
  override def numberValue() = value

  override def toString = value.toString
}