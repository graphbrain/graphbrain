package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Contexts, NodeType}
import com.graphbrain.db.{ID, EntityNode}

class VertexFun(val fun: VertexFun.VertexFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = fun match {
    case VertexFun.BuildVert => "build"
    case VertexFun.RelVert => "rel-vert"
    case VertexFun.TxtVert => "txt-vert"
  }

  override def ntype: NodeType = NodeType.Vertex

  override def vertexValue(ctxts: Contexts) = {
    fun match {
      case VertexFun.BuildVert => {
        for (p <- params) {
          p.ntype match {
            case NodeType.Vertex => p.vertexValue(ctxts)
            case NodeType.String => p.stringValue(ctxts)
            case _ => "" // error!
          }
        }

        for (c <- ctxts.ctxts) {
          val id = "(" + params.map(
            p => p.ntype match {
              case NodeType.Vertex => c.getRetVertex(p)
              case NodeType.String => c.getRetString(p)
              case _ => "" // error!
            }
          ).reduceLeft(_ + " " + _) + ")"

          c.setRetVertex(this, id)
        }
      }
      case VertexFun.RelVert => {
        val p = params(0)

        p.ntype match {
          case NodeType.Words => p.wordsValue(ctxts)
          case NodeType.String => p.stringValue(ctxts)
          case _ => "" // error!
        }

        for (c <- ctxts.ctxts) {
          p.ntype match {
            case NodeType.Words => c.setRetVertex(this, ID.reltype_id(c.getRetWords(p).text))
            case NodeType.String => c.setRetVertex(this, ID.reltype_id(c.getRetString(p)))
            case _ => "" // error!
          }
        }
      }
      case VertexFun.TxtVert => {
        val p = params(0)

        p.ntype match {
          case NodeType.Words => p.wordsValue(ctxts)
          case NodeType.String => p.stringValue(ctxts)
          case _ => "" // error!
        }

        for (c <- ctxts.ctxts) {
          p.ntype match {
            case NodeType.Words => c.setRetVertex(this, EntityNode.id("1", c.getRetWords(p).text))
            case NodeType.String => c.setRetVertex(this, EntityNode.id("1", c.getRetString(p)))
            case _ => "" // error!
          }
        }
      }
    }
  }

  override protected def typeError() = error("parameters must be variables or strings")
}

object VertexFun extends Enumeration {
  type VertexFun = Value
  val BuildVert,
    RelVert,
    TxtVert = Value
}