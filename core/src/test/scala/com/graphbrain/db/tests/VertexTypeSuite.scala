package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db.{Edge, EdgeType, VertexType, TextNode}

@RunWith(classOf[JUnitRunner])
class VertexTypeSuite extends FunSuite {

  test("global text node type") {
    val tn = TextNode("1/hank_hill", "Hank Hill", 7, 777)
    val vtype = VertexType.getType(tn.id)
    assert(vtype === VertexType.Text)
  }

  test("user text node type") {
    val tn = TextNode("user/telmo/1/hank_hill", "Hank Hill", 7, 777)
    val vtype = VertexType.getType(tn.id)
    assert(vtype === VertexType.Text)
  }

  test("global edge type type") {
    val tn = EdgeType("rtype/1/lives_in")
    val vtype = VertexType.getType(tn.id)
    assert(vtype === VertexType.EdgeType)
  }

  test("user edge type type") {
    val tn = EdgeType("user/telmo/rtype/1/lives_in")
    val vtype = VertexType.getType(tn.id)
    assert(vtype === VertexType.EdgeType)
  }

  test("global edge type") {
    val e = Edge("rtype/1/lives_in user/telmo 1/berlin")
    val vtype = VertexType.getType(e.id)
    assert(vtype === VertexType.Edge)
  }

  test("user edge type") {
    val e = Edge("user/telmo/rtype/1/lives_in user/telmo user/telmo/1/berlin")
    val vtype = VertexType.getType(e.id)
    assert(vtype === VertexType.Edge)
  }
}
