package com.graphbrain.eco

import scala.io.Source
import com.graphbrain.eco.nodes.ProgNode

class Prog(val exprs: Set[ProgNode]=Set[ProgNode]()) extends ProgNode(-1) {

  override def ntype(ctxt: Context) = NodeType.Unknown

  override def verticesValue(ctxts: Contexts, ctxt: Context): Set[String] = {
    var vertices = Set[String]()

    for (e <- exprs)
      vertices ++= e.verticesValue(ctxts, ctxt)

    vertices
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

    val ctxts = Contexts(p, s)
    p.verticesValue(ctxts, null)
    println(ctxts.sentence)
    //ctxts.print()

    /*
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

    val prog = Prog.fromNode(p.expr)
    //val ctxts = Contexts(prog, "Telmo knew Kung-Fu.")
    val ctxts = Contexts(prog, "Mrs Merkel has demanded a \"complete explanation\" of the claims, which are threatening to overshadow an EU summit.")
    prog.verticesValue(ctxts, null)
    println(prog)
    println(ctxts.sentence)
    //ctxts.print()
    */
  }
}