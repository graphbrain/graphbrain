package com.graphbrain.eco.nodes.patterns

class StrPatternElem(patt: Pattern) extends PatternElem(patt) {

  override def updateWithPrevious(prev: PatternElem) = {
    if (prev == null) {
      startMin = 0
      startMax = 0
      endMin = 0
      endMax = 0
    }
    else {
      startMin = prev.endMin + 1
      startMax = prev.endMax + 1
      endMin = startMin
      endMax = startMax
    }
  }
}
