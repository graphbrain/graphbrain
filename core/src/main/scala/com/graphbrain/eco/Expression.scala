package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode

class Expression(val root: ProgNode) {

  def eval(ctxts: Contexts): Contexts = {
    root.booleanValue(ctxts, null)
    ctxts
  }

  override def toString = root.toString

  override def equals(obj:Any) = obj match {
    case p: Expression => p.root == root
    case _ => false
  }
}

object Expression {
  def main(args: Array[String]) = {
    val p = new Parser(
      """
        (nlp test
          ((? a v b ".")
          (pos-pre v "VB")
          (let orig (txt-vert a))
          (let rel (rel-vert v))
          (let targ (txt-vert b)))
          ((! rel orig targ)))
      """)
    val ctxts = new Contexts("Telmo knew Kung-Fu.")
    //val ctxts = new Contexts("Mrs Merkel has demanded a \"complete explanation\" of the claims, which are threatening to overshadow an EU summit.")
    p.expr.eval(ctxts)
    println(p.expr)
    println(ctxts.sentence)
    ctxts.print()
  }
}