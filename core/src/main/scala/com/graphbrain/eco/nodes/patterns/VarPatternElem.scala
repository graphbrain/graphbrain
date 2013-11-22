package com.graphbrain.eco.nodes.patterns

class VarPatternElem(val name: String,
                     val possiblePOS: Array[String]=Array[String](),
                     val necessaryPOS: Array[String]=Array[String]())
  extends PatternElem {

  var intervals = List[(Int, Int)]()
  var curInterval = -1
  var intEnd = -1

  private def possibleWord(pos: Int): Boolean = {
    if (possiblePOS.length == 0)
      return true

    val wordPOS = sentence.words(pos).pos
    val b = possiblePOS.exists(p => wordPOS.startsWith(p))
    //println(pos)
    //println(sentence.words(pos))
    //println(possiblePOS.reduceLeft(_ + ", " + _))
    //println(b)
    b
  }

  private def findIntervals() = {
    intervals = List[(Int, Int)]()

    var intStart = startMin
    var intEnd = -1

    while (intStart <= startMax) {
      if (possibleWord(intStart)) {
        intEnd = intStart
        while ((intEnd <= endMax) && possibleWord(intEnd))
          intEnd += 1

        intervals = (intStart, intEnd - 1) :: intervals

        intStart = intEnd + 1
      }
      else {
        intStart += 1
      }
    }

    intervals = intervals.reverse
    //println(endMax)
    //println(intervals)
  }

  override protected def onSetSentence() = {
    if (prevElem == null) {
      startMin = 0
      startMax = 0
    }
    else {
      startMin = prevElem.endMin + 1
      startMax = prevElem.endMax + 1
    }

    val slen = sentence.length
    val remaining = elemCount - elemPos - 1

    endMin = if (remaining == 0) slen - 1 else startMin
    endMax = slen - 1 - remaining

    findIntervals()
  }

  private def curIntervalValid: Boolean = {
    if (necessaryPOS.length == 0)
      return true

    // this is wrong
    for (i <- start to end)
      if (necessaryPOS.contains(sentence.words(i).pos))
        return true

    false
  }

  override def onNext(): Boolean = {
    var found = false

    while(!found) {
      if (!step)
        return false

      if (curIntervalValid)
        found = true
    }

    //println(this)

    true
  }

  private def setCurInterval(n: Int): Boolean = {
    if (n >= intervals.length)
      return false

    curInterval = n
    intEnd = Math.min(curEndMax, intervals(curInterval)._2)

    start = Math.max(curStartMin, intervals(curInterval)._1)
    end = Math.max(curEndMin, start)

    if (end > intervals(curInterval)._2)
      return false

    true
  }

  private def step: Boolean = if (start < 0) {
    var firstInterval = 0

    // look for first interval that can fit
    while ((firstInterval < intervals.length)
      && (intervals(firstInterval)._2 < curStartMin))
      firstInterval += 1

    setCurInterval(firstInterval)
  }
  else {
    end += 1
    if (end > intEnd) {
      start += 1
      if ((start > intervals(curInterval)._2) || (start > curStartMax)) {
        if (!setCurInterval(curInterval + 1))
          return false
      }
      else {
        Math.max(curEndMin, start)
      }
    }
    true
  }

  override def toString =
    name + " = '" + sentence.slice(start, end) + "'"
}