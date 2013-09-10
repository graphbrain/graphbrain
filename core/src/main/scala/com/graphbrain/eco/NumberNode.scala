package com.graphbrain.eco

class NumberNode(val value: Double) extends ProgNode {
  override def eval(): Any = value
}