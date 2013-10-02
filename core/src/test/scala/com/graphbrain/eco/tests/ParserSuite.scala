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
      new RuleNameNode("test"),
      new CondsFun(Array(new BoolNode(true))),
      new CondsFun(Array(new SumFun(Array(new NumberNode(1), new NumberNode(1))))))))

    assert(p.prog == prog)
  }

  test("(nlp test ((? x y z)) (true))") {
    val p = new Parser("(nlp test ((? x y z)) (true))")
    assert(p.prog.toString == "(nlp test (; (? x y z)) (; true))")
  }

  test("(nlp test ((? x \"likes\" z)) (true))") {
    val p = new Parser("(nlp test ((? x \"likes\" z)) (true))")
    println(p.prog.toString)
    assert(p.prog.toString == "(nlp test (; (? x \"likes\" z)) (; true))")
  }

  test("(nlp test ((? x y z) true) (true))") {
    val p = new Parser("(nlp test ((? x y z) true) (true))")
    assert(p.prog.toString == "(nlp test (; (? x y z) true) (; true))")
  }
}