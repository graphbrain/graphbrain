package com.graphbrain.eco.nodes

abstract class VarNode(val name: String, lastTokenPos: Int) extends ProgNode(lastTokenPos) {
  override def toString = name
}