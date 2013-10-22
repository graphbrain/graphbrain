package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode

class Prog(val root: ProgNode) {

  def eval(ctxts: Contexts): Contexts = {
    val c = new Context
    ctxts.addContext(c)
    ctxts.applyChanges()
    root.booleanValue(ctxts, c)
    ctxts
  }

  override def toString = root.toString

  override def equals(obj:Any) = obj match {
    case p: Prog => p.root == root
    case _ => false
  }
}

object Prog {
  def main(args: Array[String]) = {
    val p = new Parser(
      """
        (tree test
          ((? S (NP x) (VP y z))
          (let orig (txt-vert x))
          (let rel (rel-vert y))
          (let targ (txt-vert z)))
          ((! rel orig targ)))
      """)
    val ctxts = new Contexts("Telmo likes chocolate")
    //val ctxts = new Contexts("Bitcoin can be thought of as the first real autonomous ‘corporation’ although you probably don’t see it that way.")
    p.prog.eval(ctxts)
    println(p.prog)
    println(ctxts.sentence)
    ctxts.print()
  }
}