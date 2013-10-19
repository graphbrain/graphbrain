package com.graphbrain.eco

class PTree(val pos: String, val text: String, val children: Array[PTree] = Array[PTree]()) {

  def isLeaf = children.length == 0

  override def toString: String = {
    if (children.length == 0)
      "(" + pos + " " + text + ")"
    else
      "(" + pos + " " + children.map(_.toString).reduceLeft(_ + " " + _) + ")"
  }
}
