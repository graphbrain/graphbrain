package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.NodeType

class CondsFun(params: Array[ProgNode]) extends FunNode(params) {
  override val label = ";"

  override def ntype: NodeType = {
    for (p <- params) {
      if (p.ntype != NodeType.Boolean) {
        typeError()
        return NodeType.Unknown
      }
    }
    NodeType.Boolean
  }

  override def booleanValue(): Boolean = {
    for (p <- params)
      if (!p.booleanValue()) return false
    true
  }

  override protected def typeError() = error("parameters must be boolean")
}