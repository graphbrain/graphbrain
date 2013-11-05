package com.graphbrain.eco.nodes

import com.graphbrain.eco.NodeType.NodeType
import com.graphbrain.eco.{Context, Contexts, NodeType}

class FilterFun(val fun: FilterFun.FilterFun, params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = fun match {
    case FilterFun.FilterMin => "filter-min"
    case FilterFun.FilterMax => "filter-max"
  }

  override def ntype: NodeType = NodeType.Boolean

  override def booleanValue(ctxts: Contexts) = {
    params(0).numberValue(ctxts)

    var bestCtxt: Context = null
    var bestVal = fun match {
      case FilterFun.FilterMin => Double.PositiveInfinity
      case FilterFun.FilterMin => Double.NegativeInfinity
    }

    for (c <- ctxts.ctxts) {
      val value = c.getRetNumber(params(0))

      fun match {
        case FilterFun.FilterMin =>
          if (value < bestVal) {
            bestCtxt = c
            bestVal = value
          }
        case FilterFun.FilterMax =>
          if (value > bestVal) {
            bestCtxt = c
            bestVal = value
          }
      }
    }

    for (c <- ctxts.ctxts)
      c.setRetBoolean(this, c eq bestCtxt)
  }

  override protected def typeError() = error("parameters must be boolean")
}

object FilterFun extends Enumeration {
  type FilterFun = Value
  val FilterMin,
  FilterMax = Value
}