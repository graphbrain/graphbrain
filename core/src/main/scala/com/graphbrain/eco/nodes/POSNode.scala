package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, Context, NodeType}

class POSNode(val value: String, lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  override def ntype(ctxt: Context) = NodeType.POS
  override def posValue(ctxts: Contexts, ctxt: Context) = value

  override def toString = value

  override def equals(obj:Any) = obj match {
    case p: POSNode => p.value == value
    case _ => false
  }
}