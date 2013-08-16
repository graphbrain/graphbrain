package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db._

@RunWith(classOf[JUnitRunner])
class EdgeSuite extends FunSuite {
  val g = new Graph()

  test("write edge") {
    val e = Edge("rtype/1/lives_in user/telmo 1/berlin")
    g.put(e)
  }

  test("neighbours") {
    val e = Edge("rtype/1/lives_in user/telmo 1/berlin")
    g.put(e)
    val n = g.edges("user/telmo")
    (0 to 100).foreach(println)
    n.foreach(println)
  }
}
