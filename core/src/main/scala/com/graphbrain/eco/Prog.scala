package com.graphbrain.eco

import scala.io.Source
import com.graphbrain.eco.nodes.{WWRule, WVRule, ProgNode}

class Prog(val exprs: List[ProgNode]=List[ProgNode]()) {

  def wv(s: String, depth: Integer, caller: Context=null): List[Contexts] =
    wv(Words.fromString(s), depth, caller)

  def wv(w: Words, depth: Integer, caller: Context): List[Contexts] = {
    var ctxtsList = List[Contexts]()

    for (e <- exprs) e match {
      case wv: WVRule => {
        val ctxts = Contexts(wv, this, w, depth)
        wv.vertexValue(ctxts)
        if (ctxts.ctxts.size > 0) {
          ctxtsList ::= ctxts
          return ctxtsList
        }
      }
      case _ =>
    }

    ctxtsList
  }

  def ww(w: Words, depth: Integer, caller: Context): List[Contexts] = {
    var ctxtsList = List[Contexts]()

    for (e <- exprs) e match {
      case ww: WWRule => {
        val ctxts = Contexts(ww, this, w, depth)
        ww.wordsValue(ctxts)
        if (ctxts.ctxts.size > 0) {
          ctxtsList ::= ctxts
          return ctxtsList
        }
      }
      case _ =>
    }

    ctxtsList
  }

  def parse(sentence: String): String = {
    val ctxtList = wv(sentence, 0)

    var maxStrength = Double.NegativeInfinity
    for (ctxts <- ctxtList) {
      for (c <- ctxts.ctxts) {
        val strength = c.getNumber("_strength")
        if (strength > maxStrength) maxStrength = strength
      }
    }

    for (ctxts <- ctxtList) {
      for (c <- ctxts.ctxts) {
        val strength = c.getNumber("_strength")

        if (strength == maxStrength) {
          println(ctxts.sentence)
          return c.getTopRetVertex
        }
      }
    }

    ""
  }

  override def toString = exprs.map(_.toString).reduceLeft(_ + "\n" + _)
}

object Prog {
  private def emptyLine(line: String) =
    (line == "") || (line(0) == ';')

  def load(path: String) =
    fromStringList(Source.fromFile(path).getLines().toList)

  def fromString(str: String) =
    fromStringList(str.split("\\r?\\n").toList)

  def fromStringList(strList: List[String]) = {
    var exprStr = ""
    var exprList = List[ProgNode]()

    for(line <- strList) {
      if (emptyLine(line)) {
        if (exprStr != "") {
          val p = new Parser(exprStr)
          exprList ::= p.expr
        }
        exprStr = ""
      }
      else {
        exprStr += line
      }
    }

    if (exprStr != "") {
      val p = new Parser(exprStr)
      exprList ::= p.expr
    }

    new Prog(exprList.reverse)
  }

  def fromNode(e: ProgNode) = new Prog(List[ProgNode](e))

  def main(args: Array[String]) = {
    val p = Prog.load("eco/progs/test.eco")

    println(p)

    //val s = "Telmo likes chocolate."
    //val s = "Telmo likes eating chocolate."
    //val s = "The Obama administration is appealing to its allies in Congress."
    //val s = "The Obama administration is appealing to its allies in Congress, on Wall Street and across the country to stick with President Barack Obama's health care law even as embarrassing problems with the flagship website continue to mount."
    //val s = "The Obama administration is appealing to its allies in Congress to stick with health care law."
    //val s = "The research by America's Morgan Stanley financial services firm says demand for wine exceeded supply by 300m cases in 2012"
    //val s = "Egypt's ousted leader Mohammed Morsi has gone on trial in Cairo, telling the judge the case is illegitimate as he remains president."
    val s = "Egypt's ousted leader Mohammed Morsi has gone on trial in Cairo"

    val ctxtList = p.wv(s, 0)

    /*
    var maxStrength = Double.NegativeInfinity
    for (ctxts <- ctxtList) {
      for (c <- ctxts.ctxts) {
        val strength = c.getNumber("_strength")
        if (strength > maxStrength) maxStrength = strength
      }
    }
    */

    for (ctxts <- ctxtList) {
      println(ctxts.sentence)

      for (c <- ctxts.ctxts) {
        val strength = c.getNumber("_strength")

        //if (strength == maxStrength) {
          println("\n\n")
          println(c.getTopRetVertex)
          println(strength)
          c.printCallStack()
        //}
      }
    }
  }
}