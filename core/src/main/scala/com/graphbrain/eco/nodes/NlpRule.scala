package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class NlpRule(params: Array[ProgNode]) extends FunNode(params) {
  override val label = "nlp"

  override def ntype = {
    params(0).ntype match {
      case NodeType.Boolean => NodeType.Unknown
      case _ => {
        typeError()
        NodeType.Unknown
      }
    }
  }

  override def booleanValue(ctxts: Contexts) = true

  override protected def typeError() = error("the first part of a rule must be a boolean")
}