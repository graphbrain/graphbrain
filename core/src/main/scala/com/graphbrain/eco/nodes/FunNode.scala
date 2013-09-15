package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

abstract class FunNode(prog: Prog, val params: Array[ProgNode]) extends ProgNode(prog) {
  val label: String
  override def toString = "(" + label + " " + params.mkString(" ") + ")"
}