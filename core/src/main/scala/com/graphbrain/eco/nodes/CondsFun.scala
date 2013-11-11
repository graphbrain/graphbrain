package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}

class CondsFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = ";"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    for (p <- params) {
      p.value(ctxts)
      p.ntype match {
        case NodeType.Boolean => {
          for (c <- ctxts.ctxts) {
            if (!c.getRetBoolean(p)) {
              ctxts.remContext(c)
            }
          }
          ctxts.applyChanges()
        }
      }
    }
  }

  override protected def typeError() = error("parameters must be boolean")
}