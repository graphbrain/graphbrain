package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode

class Expression(val root: ProgNode) {

  def eval(ctxts: Contexts): Contexts = {
    root.booleanValue(ctxts, null)
    ctxts
  }

  override def toString = root.toString

  override def equals(obj:Any) = obj match {
    case p: Expression => p.root == root
    case _ => false
  }
}