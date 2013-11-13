package com.graphbrain.webapp

import com.graphbrain.eco.{Context, Words, Word}

class VisualContext(ctxt: Context, val targetVertex: String="") {
  val htmlWords = renderWords(ctxt.parent.sentence)
  val htmlContext = renderContext(ctxt)
  val htmlVertex = ctxt.getTopRetVertex

  private def renderContext(ctxt: Context, indent: Int = 0): String = {
    var r = ""

    (0 until indent).foreach(x => r += "&nbsp;&nbsp;&nbsp;&nbsp;")
    r += "<span class=\"text-success\">" + ctxt.parent.rule.name + "</span>"
    r += " (" + renderWords(ctxt.parent.sentence) + ") <br />"

    for (sctxt <- ctxt.subContexts)
      r += renderContext(sctxt, indent + 1)

    r
  }

  private def renderWord(word: Word) =
    word.word + " <span class=\"text-primary\">[" + word.pos + "]</span>"

  private def renderWords(words: Words) =
    words.words.map(w => renderWord(w))
      .reduceLeft(_ + " " + _)

  def isTest = targetVertex != ""

  def correct = targetVertex == ctxt.getTopRetVertex
}
