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

  def fromExpression(e: Expression) = new Prog(Set[Expression](e))

  def main(args: Array[String]) = {
    /*
    val p = Prog.load("/Users/telmo/projects/graphbrain/test.eco")

    //val s = "Telmo likes chocolate"
    val s = "The Obama administration is appealing to its allies in Congress, on Wall Street and across the country to stick with President Barack Obama's health care law even as embarrassing problems with the flagship website continue to mount."

    val ctxts = new Contexts(s)
    p.eval(ctxts)
    println(ctxts.sentence)
    ctxts.print()
    */

    val p = new Parser(
      """
        (nlp test
          ((? a v b ".")
          (is-pos-pre v "VB")
          (let orig (txt-vert a))
          (let rel (rel-vert v))
          (let targ (txt-vert b)))
          ((! rel orig targ)))
      """)

    val prog = Prog.fromExpression(p.expr)
    val ctxts = new Contexts(prog, "Telmo knew Kung-Fu.")
    //val ctxts = new Contexts("Mrs Merkel has demanded a \"complete explanation\" of the claims, which are threatening to overshadow an EU summit.")
    prog.eval(ctxts)
    println(prog)
    println(ctxts.sentence)
    ctxts.print()
  }
}