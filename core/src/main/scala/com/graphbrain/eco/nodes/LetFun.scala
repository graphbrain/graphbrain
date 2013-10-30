package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

class LetFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "let"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0) match {
      case v: VarNode => {
        for (c <- ctxts.ctxts) {
          val p = params(1)
          p.ntype match {
            case NodeType.Boolean => {
              p.booleanValue(ctxts)
              c.setBoolean(v.name, c.getRetBoolean(p))
            }
            case NodeType.Number => {
              p.numberValue(ctxts)
              c.setNumber(v.name, c.getRetNumber(p))
            }
            case NodeType.Words => {
              p.wordsValue(ctxts)
              c.setWords(v.name, c.getRetWords(p))
            }
            case NodeType.String => {
              p.stringValue(ctxts)
              c.setString(v.name, c.getRetString(p))
            }
            case NodeType.Vertex => {
              p.vertexValue(ctxts)
              c.setVertex(v.name, c.getRetVertex(p))
            }
          }
        }
      }
    }
  }

  override protected def typeError() = error("parameters must be boolean")
}