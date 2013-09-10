package com.graphbrain.eco

class VarNode(prog: Prog, val name: String) extends ProgNode(prog) {
  override def eval(): AnyVal = prog.varValue(name)
}