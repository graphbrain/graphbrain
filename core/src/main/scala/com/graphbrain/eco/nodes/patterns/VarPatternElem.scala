package com.graphbrain.eco.nodes.patterns

class VarPatternElem(patt: Pattern) extends PatternElem(patt) {

  override def updateWithPrevious(prev: PatternElem) = {
    val slen = patt.sentence.length

    if (prev == null) {
      startMin = 0
      startMax = 0
      endMin = slen - 1
      endMax = slen - 1
    }
    else {
      startMin = prev.endMin + 1
      startMax = prev.endMax + 1
      endMin = slen - 1
      endMax = slen - 1
    }
  }
}
