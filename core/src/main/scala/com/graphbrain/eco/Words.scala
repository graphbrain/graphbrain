package com.graphbrain.eco

class Words(val words: Array[Word]) {
  def text = words.map(_.word).reduceLeft(_ + " " + _)
}
