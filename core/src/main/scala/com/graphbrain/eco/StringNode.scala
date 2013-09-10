package com.graphbrain.eco

class StringNode(prog: Prog, val value: String) extends ProgNode(prog) {
  override def eval(): Any = value
}