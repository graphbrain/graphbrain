package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode

class Prog(val root: ProgNode) {
  val vars = Map[String, AnyVal]()

  def varValue(name: String) = vars(name)

  def eval() = root.eval()
}