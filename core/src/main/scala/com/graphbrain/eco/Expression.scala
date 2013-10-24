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
          ((? x "likes" y ".")
          (let orig (txt-vert x))
          (let rel (rel-vert "likes"))
          (let targ (txt-vert y)))
          ((! rel orig targ)))
      """)
    val ctxts = new Contexts("Telmo likes chocolate.")
    //val ctxts = new Contexts("Mrs Merkel has demanded a \"complete explanation\" of the claims, which are threatening to overshadow an EU summit.")
    p.expr.eval(ctxts)
    println(p.expr)
    println(ctxts.sentence)
    ctxts.print()
  }
}