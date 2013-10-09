package com.graphbrain.eco

import com.graphbrain.eco.POS.POS

class Word(val word: String, val pos: POS, val lemma: String) {
  override def toString = word + " [" + pos + ", " + lemma + "]"
}