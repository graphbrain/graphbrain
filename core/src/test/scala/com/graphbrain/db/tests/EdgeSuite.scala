package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db._

@RunWith(classOf[JUnitRunner])
class EdgeSuite extends FunSuite {
  val g = new Graph()

  test("write edge") {
    val telmo = TextNode("1/telmo_menezes")
    val berlin = TextNode("1/berlin")
    val livesIn = EdgeType("rtype/1/lives_in", "lives in")
    g.put(telmo)
    g.put(berlin)
    g.put(livesIn)
    val e = Edge("rtype/1/lives_in 1/telmo_menezes 1/berlin")
    g.put(e)
  }

  test("neighbours") {
    val e = Edge("rtype/1/lives_in 1/telmo_menezes 1/berlin")
    g.put(e)
    val n = g.edges("1/telmo_menezes")
  }
}
