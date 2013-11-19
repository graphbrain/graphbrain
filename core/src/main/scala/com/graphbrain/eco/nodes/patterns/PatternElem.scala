package com.graphbrain.eco.nodes.patterns

abstract class PatternElem(val patt: Pattern) {
  var startMin: Int = -1
  var startMax: Int = -1
  var endMin: Int = -1
  var endMax: Int = -1

  def updateWithPrevious(prev: PatternElem)
}
