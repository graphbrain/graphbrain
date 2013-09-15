package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType

class NlpRule(params: Array[ProgNode]) extends FunNode(params) {
  override val label = "nlp"

  override val ntype = {
    params(0).ntype match {
      case NodeType.Number => params(1).ntype match {
        case NodeType.Number => NodeType.Number
        case _ => {
          typeError()
          NodeType.Unknown
        }
      }
      case _ => {
        typeError()
        NodeType.Unknown
      }
    }
  }

  override def booleanValue() = true

  override protected def typeError() = error("the first part of a rule must be a boolean")
}