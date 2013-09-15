package com.graphbrain.eco.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.eco._
import com.graphbrain.eco.nodes._

@RunWith(classOf[JUnitRunner])
class ParserSuite extends FunSuite {
  test("nlp test: 1 + 1 -> 2 + 2") {
    val p = new Parser("nlp test: 1 + 1 -> 2 + 2")
    val prog: Prog = new Prog(new NlpRule(Array(
      new SumFun(Array(new NumberNode(1), new NumberNode(1))),
      new SumFun(Array(new NumberNode(2), new NumberNode(2))))))

    assert(p.prog == prog)
  }
}