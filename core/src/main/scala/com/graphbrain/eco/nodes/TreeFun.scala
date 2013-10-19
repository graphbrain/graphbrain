package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class TreeFun(val fun: TreeFun.TreeFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {

  override val label = fun match {
    case TreeFun.Is => "is"
    case TreeFun.IsLeaf => "is-leaf"
  }

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = {
    fun match {
      case TreeFun.Is => {
        params(0) match {
          case t: TreeVar =>
            t.treeValue(ctxts, ctxt).pos == params(1).stringValue(ctxts, ctxt)
          case _ => false
        }
      }
      case TreeFun.IsLeaf => {
        params(0) match {
          case t: TreeVar => t.treeValue(ctxts, ctxt).isLeaf
          case _ => false
        }
      }
    }
  }

  override protected def typeError() = error("parameters must be variables or strings")
}

object TreeFun extends Enumeration {
  type TreeFun = Value
  val Is,
  IsLeaf = Value
}