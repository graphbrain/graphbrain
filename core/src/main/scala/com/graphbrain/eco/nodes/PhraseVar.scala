package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Word, Contexts, Context, NodeType}

class PhraseVar(name: String, val value: Array[Word], lastTokenPos: Int= -1) extends VarNode(name, lastTokenPos) {
  override def ntype = NodeType.Phrase
  override def phraseValue(ctxts: Contexts, ctxt: Context): Array[Word] = value
  override def stringValue(ctxts: Contexts, ctxt: Context) = value.map(_.word).reduceLeft(_ + " " + _)

  override def equals(obj:Any) = obj match {
    case p: PhraseVar => p.name == name
    case _ => false
  }
}