package com.graphbrain.eco.nodes

abstract class FunNode(val params: Array[ProgNode]) extends ProgNode {
  val label: String
  override def toString = "(" + label + " " + params.mkString(" ") + ")"

  override def equals(obj:Any) = obj match {
    case f: FunNode => params.sameElements(f.params)
    case _ => false
  }
}