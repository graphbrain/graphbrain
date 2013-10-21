package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class VertexFun(val fun: VertexFun.VertexFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = fun match {
    case VertexFun.RelVert => "rel-vert"
    case VertexFun.TxtVert => "txt-vert"
  }

  override def ntype(ctxt: Context): NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = {
    fun match {
      case VertexFun.RelVert => {
        params(0) match {
          case t: VarNode =>
            t.treeValue(ctxts, ctxt).pos == params(1).stringValue(ctxts, ctxt)
          case _ => false
        }
      }
      case VertexFun.TxtVert => {
        params(0) match {
          case t: VarNode => t.treeValue(ctxts, ctxt).isLeaf
          case _ => false
        }
      }
    }
  }

  override protected def typeError() = error("parameters must be variables or strings")
}

object VertexFun extends Enumeration {
  type VertexFun = Value
  val RelVert,
  TxtVert = Value
}