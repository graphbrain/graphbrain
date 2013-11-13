package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db.{EntityNode, Edge, EdgeType}

@RunWith(classOf[JUnitRunner])
class GlobalAndUserSuite extends FunSuite {

  test("global text node to user") {
    val tn = EntityNode("1/hank_hill", 7, 777)
    val utn = tn.toUser("user/telmo")
    assert(utn.id === "user/telmo/1/hank_hill")
  }

  test("user text node to global") {
    val tn = EntityNode("user/telmo/1/hank_hill", 7, 777)
    val gtn = tn.toGlobal
    assert(gtn.id === "1/hank_hill")
  }

  test("global edge type to user") {
    val et = EdgeType("rtype/1/lives_in")
    val uet = et.toUser("user/telmo")
    assert(uet.id === "user/telmo/rtype/1/lives_in")
  }

  test("user edge type to global") {
    val et = EdgeType("user/telmo/rtype/1/lives_in")
    val uet = et.toGlobal
    assert(uet.id === "rtype/1/lives_in")
  }

  test("global edge to user") {
    val e = Edge("rtype/1/lives_in user/telmo 1/berlin")
    val ue = e.toUser("user/telmo")
    assert(ue.id === "user/telmo/rtype/1/lives_in user/telmo user/telmo/1/berlin")
  }
}