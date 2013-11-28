package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db._

@RunWith(classOf[JUnitRunner])
class LevelDbBackendSuite extends FunSuite {
  val back = new LevelDbBackend()

  test("write edge") {
    val e = Edge.fromId("r/1/lives_in user/telmo 1/berlin")
    back.put(e)

    val e2 = back.get("r/1/lives_in user/telmo 1/berlin", VertexType.Edge)
    assert(e.id === e2.id)
  }

  test("neighbours") {
    val hank = EntityNode("1/hank_hill")
    val texas = EntityNode("1/texas")
    val livesIn = EdgeType("r/1/lives_in", "lives in")
    back.put(hank)
    back.put(texas)
    back.put(livesIn)
    val e = new Edge(Array[Vertex](livesIn, hank, texas))
    back.put(e)

    val nb = back.edges(hank)
    assert(nb.head.id === "r/1/lives_in 1/hank_hill 1/texas")
  }
}
