package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class LetFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "let"

  override def ntype(ctxt: Context): NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = {
    if (ctxt != null) {
    params(0) match {
      case v: VarNode =>
        params(1).ntype(ctxt) match {
          case NodeType.Boolean => ctxt.setBoolean(v.name, params(1).booleanValue(ctxts, ctxt))
          case NodeType.Number => ctxt.setNumber(v.name, params(1).numberValue(ctxts, ctxt))
          case NodeType.POS => ctxt.setPOS(v.name, params(1).posValue(ctxts, ctxt))
          case NodeType.Words => ctxt.setWords(v.name, params(1).wordsValue(ctxts, ctxt))
          case NodeType.String => ctxt.setString(v.name, params(1).stringValue(ctxts, ctxt))
          case NodeType.Vertex => ctxt.setVertex(v.name, params(1).vertexValue(ctxts, ctxt))
        }
    }
    }

    true
  }

  override protected def typeError() = error("parameters must be boolean")
}