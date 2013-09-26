package com.graphbrain.eco.nodes

abstract class FunNode(val params: Array[ProgNode], lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {
  val label: String

  override def toString = "(" + label + " " + params.mkString(" ") + ")"

  override def equals(obj:Any) = obj match {
    case f: FunNode => params.sameElements(f.params)
    case _ => false
  }
}