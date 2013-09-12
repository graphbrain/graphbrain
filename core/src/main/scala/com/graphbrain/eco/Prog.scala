package com.graphbrain.eco

import com.graphbrain.eco.nodes.{VarNode, ProgNode}

class Prog(val root: ProgNode) {
  val vars = Map[String, VarNode]()
}