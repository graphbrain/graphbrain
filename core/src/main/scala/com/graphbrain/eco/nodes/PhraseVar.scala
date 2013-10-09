package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class PhraseVar(name: String, lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.Phrase

  override def equals(obj:Any) = obj match {
    case p: PhraseVar => p.name == name
    case _ => false
  }
}