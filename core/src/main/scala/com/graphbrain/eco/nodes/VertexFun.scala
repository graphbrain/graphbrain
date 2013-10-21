package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}
import com.graphbrain.db.{ID, TextNode}

class VertexFun(val fun: VertexFun.VertexFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = fun match {
    case VertexFun.RelVert => "rel-vert"
    case VertexFun.TxtVert => "txt-vert"
  }

  override def ntype(ctxt: Context): NodeType = NodeType.Vertex

  override def vertexValue(ctxts: Contexts, ctxt: Context): String = {
    fun match {
      case VertexFun.RelVert => {
        params(0).ntype(ctxt) match {
          case NodeType.PTree => ID.reltype_id(params(0).treeValue(ctxts, ctxt).text)
          case NodeType.String => ID.reltype_id(params(0).stringValue(ctxts, ctxt))
          case _ => "" // error!
        }
      }
      case VertexFun.TxtVert => {
        params(0).ntype(ctxt) match {
          case NodeType.PTree => TextNode.id("1", params(0).treeValue(ctxts, ctxt).text)
          case NodeType.String => TextNode.id("1", params(0).stringValue(ctxts, ctxt))
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