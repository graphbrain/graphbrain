package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db.{EntityNode, Graph}

@RunWith(classOf[JUnitRunner])
class GraphSuite extends FunSuite {

  val g = new Graph()

  test("add text node") {
    val tn = EntityNode("1/hank_hill", 7, 777)
    g.remove(tn)
    g.put(tn)

    g.get("1/hank_hill") match {
      case t: EntityNode => {
        assert(t.id === "1/hank_hill")
        assert(t.degree === 7)
        assert(t.ts === 777)
      }
      case _ => assert(condition = false)
    }
  }

  test("get text node that does not exist") {
    val tn = EntityNode("1/hank_hill", 7, 777)
    g.remove(tn)
    assert(g.get("1/hank_hill") == null)
  }
}
