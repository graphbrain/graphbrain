package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class VertexFun(val fun: VertexFun.VertexFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = fun match {
    case VertexFun.RelVert => "rel-vert"
    case VertexFun.TxtVert => "txt-vert"
  }

  override def ntype(ctxt: Context): NodeType = NodeType.Vertex

  override def vertexValue(ctxts: Contexts, ctxt: Context): String = {
    fun match {
      case VertexFun.RelVert => {
        params(0) match {
          case t: VarNode => "rel/1/test"
          case _ => "" // error!
        }
      }
      case VertexFun.TxtVert => {
        params(0) match {
          case t: VarNode => "1/test"
          case _ => "" // error!
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