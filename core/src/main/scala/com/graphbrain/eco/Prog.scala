package com.graphbrain.eco

import scala.io.Source
import com.graphbrain.eco.nodes.{WWRule, WVRule, ProgNode}

class Prog(val exprs: Set[ProgNode]=Set[ProgNode]()) {

  def wv(s: String): List[Contexts] = {
    val w = Words.fromString(s)
    wv(w)
  }

  def wv(w: Words): List[Contexts] = {
    var ctxtsList = List[Contexts]()

    for (e <- exprs) e match {
      case wv: WVRule => {
        val ctxts = Contexts(this, w)
        wv.vertexValue(ctxts)
        ctxtsList ::= ctxts
      }
      case _ =>
    }

    ctxtsList
  }

  def ww(w: Words): List[Contexts] = {
    var ctxtsList = List[Contexts]()

    for (e <- exprs) e match {
      case ww: WWRule => {
        val ctxts = Contexts(this, w)
        ww.wordsValue(ctxts)
        ctxtsList ::= ctxts
      }
      case _ =>
    }

    ctxtsList
  }

  override def toString = exprs.map(_.toString).reduceLeft(_ + "\n" + _)
}

object Prog {
  def load(path: String) = {
    var exprStr = ""
    var exprList = List[ProgNode]()

    for(line <- Source.fromFile(path).getLines()) {
      if (line == "") {
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

    new Prog(exprList.reverse.toSet)
  }

  def fromNode(e: ProgNode) = new Prog(Set[ProgNode](e))

  def main(args: Array[String]) = {
    val p = Prog.load("/Users/telmo/projects/graphbrain/test.eco")

    //val s = "Telmo likes chocolate."
    //val s = "Telmo likes eating chocolate."
    //val s = "The Obama administration is appealing to its allies in Congress."
    //val s = "The Obama administration is appealing to its allies in Congress, on Wall Street and across the country to stick with President Barack Obama's health care law even as embarrassing problems with the flagship website continue to mount."
    val s = "The Obama administration is appealing to its allies in Congress to stick with health care law."

    val ctxts = p.wv(s)
    println(ctxts)
  }
}