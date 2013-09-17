package com.graphbrain.eco

import com.graphbrain.eco.nodes.VarNode
import scala.collection.mutable

class Context {
  val vars = mutable.Map[String, VarNode]()
}