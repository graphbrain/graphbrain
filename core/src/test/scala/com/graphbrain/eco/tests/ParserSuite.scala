package com.graphbrain.eco.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.eco._
import com.graphbrain.eco.nodes._

@RunWith(classOf[JUnitRunner])
class ParserSuite extends FunSuite {
  test("(nlp test (true) ((+ 1 1)))") {
    val p = new Parser("(nlp test (true) ((+ 1 1)))")
    val prog: Prog = new Prog(new NlpRule(Array(
      new StringNode("test"),
      new CondsFun(Array(new BoolNode(true))),
      new CondsFun(Array(new SumFun(Array(new NumberNode(1), new NumberNode(1))))))))

    assert(p.prog == prog)
  }

  /*
  test("nlp test: true -> 1 + 2 * 3") {
    val p = new Parser("nlp test: true -> 1 + 2 * 3")
    val prog: Prog = new Prog(new NlpRule(Array(
      new BoolNode(true),
      new SumFun(Array(new NumberNode(1), new MulFun(Array(new NumberNode(2), new NumberNode(3))))))))

    assert(p.prog == prog)
  }

  test("nlp test: false -> (1 + 2) * 3") {
    val p = new Parser("nlp test: false -> (1 + 2) * 3")
    val prog: Prog = new Prog(new NlpRule(Array(
      new BoolNode(false),
      new MulFun(Array(new SumFun(Array(new NumberNode(1), new NumberNode(2))), new NumberNode(3))))))

    assert(p.prog == prog)
  }

  test("nlp test: 'x y z' -> true") {
    val p = new Parser("nlp test: 'x y z' -> true")
    assert(p.prog.toString == "(nlp (' x y z) true)")
  }

  test("nlp test: 'x \"likes\" z' -> true") {
    val p = new Parser("nlp test: 'x \"likes\" z' -> true")
    assert(p.prog.toString == "(nlp (' x likes z) true)")
  }

  test("nlp test: 'x y z'; true -> true") {
    val p = new Parser("nlp test: 'x y z'; true -> true")
    assert(p.prog.toString == "(nlp (; (' x y z) true) true)")
  }
  */
}