package com.graphbrain.eco.nodes

abstract class VarNode(val name: String) extends ProgNode {
  override def toString = name
}