package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}

class SubFun(params: Array[ProgNode]) extends FunNode(params) {
  override val label = "-"

  override def ntype = {
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

  override def numberValue(ctxts: Contexts) = params(0).numberValue(ctxts) - params(1).numberValue(ctxts)

  override protected def typeError() = error("parameters must be two numbers")
}