package com.graphbrain.eco

import com.graphbrain.eco.nodes.ProgNode

class Prog(val root: ProgNode) {

  def eval(ctxts: Contexts): Contexts = {
    root.booleanValue(ctxts, null)
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
    val p = new Parser("""(tree test ((? x y) (is x "NP") (is y "VP")) (true))""")
    val ctxts = new Contexts("Telmo is a hacker")
    p.prog.eval(ctxts)
    println(p.prog)
    println(ctxts.sentence)
    ctxts.print()
  }
}