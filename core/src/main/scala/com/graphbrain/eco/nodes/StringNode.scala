package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

class StringNode(prog: Prog, val value: String) extends ProgNode(prog) {
  override def eval(): AnyVal = value
}