package com.graphbrain.eco.nodes

abstract class RuleNode(params: Array[ProgNode], lastTokenPos: Int= -1)
  extends FunNode(params, lastTokenPos) {

  val name = params(0) match {
    case p: PatFun => p.params.map(_.toString).reduceLeft(_ + "-" + _)
    case _ => "" // error
  }
}