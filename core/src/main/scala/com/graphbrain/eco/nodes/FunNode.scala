package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

class FunNode(prog: Prog, val name: String, val params: Array[ProgNode]) extends ProgNode(prog) {
  override def eval(): AnyVal = name match {
    case "+" => sum
    case _ => 0 // TODO: error
  }

  def sum = {

  }
}