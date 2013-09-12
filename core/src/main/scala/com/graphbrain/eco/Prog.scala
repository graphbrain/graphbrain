package com.graphbrain.eco

import com.graphbrain.eco.nodes.{VarNode, ProgNode}

class Prog(val root: ProgNode) {
  val vars = Map[String, VarNode]()

  def varValue(name: String) = vars(name).value

  def eval() = root.eval()
}