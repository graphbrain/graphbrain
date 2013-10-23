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
    val p = Prog.load("/Users/telmo/test.eco")
    println(p)
  }
}