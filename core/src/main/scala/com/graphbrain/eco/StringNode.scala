package com.graphbrain.eco

class StringNode(val value: String) extends ProgNode {
  override def eval(): Any = value
}