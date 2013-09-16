package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.NodeType

class PatFun(params: Array[ProgNode]) extends FunNode(params) {
  override val label = "'"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(): Boolean = false

  override protected def typeError() = error("parameters must be variables or strings")
}