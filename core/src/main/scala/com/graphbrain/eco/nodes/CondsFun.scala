package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

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

  override def booleanValue(ctxts: Contexts): Boolean = {
    for (p <- params)
      if (!p.booleanValue(ctxts)) return false
    true
  }

  override protected def typeError() = error("parameters must be boolean")
}