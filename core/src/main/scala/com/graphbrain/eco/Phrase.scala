package com.graphbrain.eco

class Phrase(val pos: String, val text: String, val children: Array[Phrase] = Array[Phrase]()) {
  override def toString: String = {
    if (children.length == 0)
      "(" + pos + " " + text + ")"
    else
      "(" + pos + " " + children.map(_.toString).reduceLeft(_ + " " + _) + ")"
  }
}
