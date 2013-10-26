package com.graphbrain.eco

import scala.io.Source
import com.graphbrain.eco.nodes.{WWRule, WVRule, ProgNode}

class Prog(val exprs: Set[ProgNode]=Set[ProgNode]()) {

  def wv(s: String): Set[String] = {
    var vertices = Set[String]()

    for (e <- exprs) e match {
      case wv: WVRule => {
        val ctxts = Contexts(this, s)
        vertices ++= wv.verticesValue(ctxts, null)
      }
      case _ =>
    }

    vertices
  }

  def wv(w: Words): Set[String] = {
    var vertices = Set[String]()

    for (e <- exprs) e match {
      case wv: WVRule => {
        val ctxts = Contexts(this, w)
        vertices ++= wv.verticesValue(ctxts, null)
      }
      case _ =>
    }

    vertices
  }

  def ww(w: Words): Words = {
    var r = Words.empty

    for (e <- exprs) e match {
      case ww: WWRule => {
        val ctxts = Contexts(this, w)
        r = ww.wordsValue(ctxts, null)
      }
      case _ =>
    }

    r
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

    val s = "Telmo likes chocolate."
    //val s = "The Obama administration is appealing to its allies in Congress, on Wall Street and across the country to stick with President Barack Obama's health care law even as embarrassing problems with the flagship website continue to mount."

    p.wv(s)
  }
}