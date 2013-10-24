package com.graphbrain.eco

import scala.io.Source

class Prog(val exprs: Set[Expression]=Set[Expression]()) {

  def eval(ctxts: Contexts): Contexts = {
    for (e <- exprs) e.eval(ctxts)
    ctxts
  }

  override def toString = exprs.map(_.toString).reduceLeft(_ + "\n" + _)
}

object Prog {
  def load(path: String) = {
    var exprStr = ""
    var exprList = List[Expression]()

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

  def main(args: Array[String]) = {
    val p = Prog.load("/Users/telmo/projects/graphbrain/test.eco")

    //val s = "Telmo likes chocolate"
    val s = "The Obama administration is appealing to its allies in Congress, on Wall Street and across the country to stick with President Barack Obama's health care law even as embarrassing problems with the flagship website continue to mount."

    val ctxts = new Contexts(s)
    p.eval(ctxts)
    println(ctxts.sentence)
    ctxts.print()
  }
}