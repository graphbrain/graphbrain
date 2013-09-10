package com.graphbrain.eco

class NumberNode(prog: Prog, val value: Double) extends ProgNode(prog) {
  override def eval(): AnyVal = value
}