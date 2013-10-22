package com.graphbrain.eco

class Prog(val exprs: Set[Expression]) {

  def eval(ctxts: Contexts): Contexts = {
    for (e <- exprs) e.eval(ctxts)
    ctxts
  }

  override def toString = exprs.map(_.toString).reduceLeft(_ + "\n" + _)
}

object Prog {
  def main(args: Array[String]) = {

  }
}