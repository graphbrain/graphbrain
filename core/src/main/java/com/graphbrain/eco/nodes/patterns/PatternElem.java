package com.graphbrain.eco.nodes.patterns

import com.graphbrain.eco.Words

abstract class PatternElem extends Ordered[PatternElem] {
  var elemPos: Int = -1
  var elemCount: Int = -1

  var fixed = false
  var preProcessed = false
  var sentence: Words = null

  var prevElem: PatternElem = null
  var nextElem: PatternElem = null

  var start: Int = -1
  var end: Int = -1

  var startMin: Int = -1
  var startMax: Int = -1
  var endMin: Int = -1
  var endMax: Int = -1

  def init(elemPos: Int, elemCount: Int, prev: PatternElem, next: PatternElem) = {
    this.elemPos = elemPos
    this.elemCount = elemCount
    this.prevElem = prev
    this.nextElem = next
  }

  protected def priority: Int

  protected def onSetSentence()

  def setSentence(sentence: Words) = {
    this.sentence = sentence
    preProcessed = false
    onSetSentence()
  }

  def rewind() =
    start = -1

  def preProcess() = {
    preProcessed = true
    onPreProcess()
  }

  protected def onPreProcess() = {}

  def next(): Boolean = {
    if (!preProcessed)
      preProcess()

    onNext()
  }

  def onNext(): Boolean

  def curStartMin = if ((prevElem != null) && prevElem.fixed)
    prevElem.end + 1
  else
    startMin

  def curStartMax = if ((prevElem != null) && prevElem.fixed)
    prevElem.end + 1
  else
    startMax

  def curEndMin = if ((nextElem != null) && nextElem.fixed)
    nextElem.start - 1
  else
    endMin

  def curEndMax = if ((nextElem != null) && nextElem.fixed)
    nextElem.start - 1
  else
    endMax

  def compare(that: PatternElem) =
    that.priority - this.priority
}
