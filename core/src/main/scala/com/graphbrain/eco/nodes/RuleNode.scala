package com.graphbrain.eco.nodes

abstract class RuleNode(val name: String, params: Array[ProgNode], lastTokenPos: Int= -1)
  extends FunNode(params, lastTokenPos) {

  override def toString = "(" + label + " " + name + " " + params.mkString(" ") + ")"
}