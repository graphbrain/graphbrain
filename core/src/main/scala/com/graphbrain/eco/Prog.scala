package com.graphbrain.eco

import com.graphbrain.eco.nodes.{VarNode, ProgNode}

class Prog(val root: ProgNode) {
  val vars = Map[String, VarNode]()

  override def toString = root.toString

  override def equals(obj:Any) = obj match {
    case p: Prog => p.root == root
    case _ => false
  }
}