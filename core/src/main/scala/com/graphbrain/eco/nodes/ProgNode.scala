package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog

abstract class ProgNode(val prog: Prog) {
  def eval(): AnyVal
}