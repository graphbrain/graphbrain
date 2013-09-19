package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Context, Contexts, NodeType}

class DivFun(params: Array[ProgNode]) extends FunNode(params) {
  override val label = "/"

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

  override def numberValue(ctxts: Contexts, ctxt: Context) =
    params(0).numberValue(ctxts, ctxt) / params(1).numberValue(ctxts, ctxt)

  override protected def typeError() = error("parameters must be two numbers")
}