package com.graphbrain.eco.nodes

abstract class ListNode(val params: Array[ProgNode], lastTokenPos: Int= -1) extends ProgNode(lastTokenPos) {

  override def toString = "(" + params.mkString(" ") + ")"

  override def equals(obj:Any) = obj match {
    case l: ListNode => params.sameElements(l.params)
    case _ => false
  }
}