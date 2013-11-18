package com.graphbrain.eco

import com.graphbrain.nlp.Lemmatiser

class Words(val words: Array[Word]=Array[Word](), val pos: Int=0) {
  def text = words.map(_.word).reduceLeft(_ + " " + _)

  def count = words.size

  def +(operand: Words): Words =
    new Words(words ++ operand.words)

  override def toString =
    if (words.length > 0) words.map(_.toString).reduceLeft(_ + " " + _) else ""

  def endsWith(words2: Words): Boolean = {
    val length1 = words.length
    val length2 = words2.words.length

    if (length2 > length1)
      return false
    if (length1 == 0)
      return false
    if (length2 == 0)
      return false

    for (i <- 0 until length2)
      if (words2.words(length2 - i - 1).word != words(length1 - i - 1).word)
        return false

    true
  }

  def removeFullStop() = {
    if (words.last.word == ".")
      new Words(words.dropRight(1), pos)
    else
      this
  }
}

object Words {
  val l = new Lemmatiser

  def empty = new Words()

  def fromString(s: String) =
    new Words(l.annotate(s).map(w => new Word(w._1, w._2, w._3)).toArray)
}
