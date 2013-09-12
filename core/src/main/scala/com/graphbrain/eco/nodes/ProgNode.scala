package com.graphbrain.eco.nodes

import com.graphbrain.eco.Prog
import com.graphbrain.eco.NodeType.NodeType

abstract class ProgNode(val prog: Prog) {
  def eval(): Any

  def ntype: NodeType
}