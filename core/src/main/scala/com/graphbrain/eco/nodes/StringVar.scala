package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, Context, NodeType}

class StringVar(name: String, val value: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.String
  override def stringValue(ctxts: Contexts, ctxt: Context) = value

  override def equals(obj:Any) = obj match {
    case s: StringVar => s.name == name
    case _ => false
  }
}