package com.graphbrain.eco

class Words(val words: Array[Word]) {
  def text = words.map(_.word).reduceLeft(_ + " " + _)

  override def toString = words.map(_.toString).reduceLeft(_ + " " + _)
}
