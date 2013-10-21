package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{PTree, Context, Contexts, NodeType}

class PatFun(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = "?"

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts, ctxt: Context): Boolean = {
    val newContext = new Context

    if (booleanValue(newContext, ctxts.sentence)) {
      ctxts.addContext(newContext)
      true
    }
    else
      false
  }

  def booleanValue(newContext: Context, pt: PTree): Boolean = {

    val words = pt.children.length
    val pcount = params.length

    val start = params(0) match {
      case p: POSNode => {
        if (p.value != pt.pos)
          return false
        1
      }
      case _ => 0
    }

    // leaf case
    if ((pcount - start == 1) && pt.isLeaf) {
      params(start) match {
        case v: TreeVar => newContext.setPhrase(v.name, pt)
      }
    }
    // other cases
    else {
    if (words != (pcount - start)) return false

    for (i <- start until pcount) {
      val j = i - start
      params(i) match {
        case s: StringNode =>
          if (s.value != pt.children(j).text)
            return false
        case p: POSNode =>
          if (p.value != pt.pos)
            return false
        case p: PatFun =>
          if (!p.booleanValue(newContext, pt.children(j)))
            return false
        case _ =>
      }
    }

    for (i <- start until pcount) {
      val j = i - start
      params(i) match {
        case v: TreeVar =>
          newContext.setPhrase(v.name, pt.children(j))
        case _ =>
      }
    }
    }

    true
  }

  override protected def typeError() = error("parameters must be variables or strings")
}