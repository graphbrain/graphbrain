package com.graphbrain.eco

class Word(val word: String, val pos: String, val lemma: String) {
  override def toString() = word + " [" + pos + ", " + lemma + "]"
}