package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

abstract class VarNode(prog: Prog, val name: String) extends ProgNode(prog) {
  def value: Any

  def eval() = value
}