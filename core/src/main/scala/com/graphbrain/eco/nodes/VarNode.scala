package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

class VarNode(prog: Prog, val name: String) extends ProgNode(prog) {
  override def eval(): AnyVal = prog.varValue(name)
}