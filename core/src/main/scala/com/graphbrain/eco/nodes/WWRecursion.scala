package com.graphbrain.eco.nodes

import com.graphbrain.eco.{Contexts, NodeType}
import com.graphbrain.eco.NodeType.NodeType

class WWRecursion(params: Array[ProgNode], lastTokenPos: Int= -1) extends FunNode(params, lastTokenPos) {
  override val label = ":ww"

  override def ntype: NodeType = NodeType.Words

  override def wordsValue(ctxts: Contexts) = {
    params(0).wordsValue(ctxts)
    for (c <- ctxts.ctxts) {
      val newCtxts = ctxts.prog.ww(c.getRetWords(params(0)))

      for (nctxts <- newCtxts) {
        for (nc <- nctxts.ctxts) {
          val forkCtxt = c.clone()
          forkCtxt.setRetWords(this, nc.getTopRetWords)
          forkCtxt.addSubContext(nc)

          // add forked context to caller contexts
          ctxts.addContext(forkCtxt)
        }
      }

      // remove original context
      ctxts.remContext(c)
    }
    ctxts.applyChanges()
  }
}
