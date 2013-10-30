package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

class LetFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "let"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0) match {
      case v: VarNode => {
        val p = params(1)
        p.value(ctxts)
        for (c <- ctxts.ctxts) {
          p.ntype match {
            case NodeType.Boolean => c.setBoolean(v.name, c.getRetBoolean(p))
            case NodeType.Number => c.setNumber(v.name, c.getRetNumber(p))
            case NodeType.Words => c.setWords(v.name, c.getRetWords(p))
            case NodeType.String => c.setString(v.name, c.getRetString(p))
            case NodeType.Vertex => c.setVertex(v.name, c.getRetVertex(p))
          }
          c.setRetBoolean(this, value = true)
        }
      }
    }
  }

  override protected def typeError() = error("parameters must be boolean")
}