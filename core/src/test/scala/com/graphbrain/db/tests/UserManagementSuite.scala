package com.graphbrain.db.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.db.{UserManagement, Graph}

@RunWith(classOf[JUnitRunner])
class UserManagementSuite extends FunSuite {

  val g = new Graph() with UserManagement

  test("create user") {
    g.createUser(username="turing",
                           name="Alan Turing",
                           email="turing@graphbrain.com",
                           password="test123",
                           role="user")


    assert(g.usernameExists("turing"))
    assert(g.emailExists("turing@graphbrain.com"))
  }

  test("login with username") {
    val usr = g.attemptLogin("turing", "test123")
    assert(usr != null)
    assert(usr.id === "user/turing")
  }

  test("login with email") {
    val usr = g.attemptLogin("turing@graphbrain.com", "test123")
    assert(usr != null)
    assert(usr.id === "user/turing")
  }

  test("fail login") {
    val usr = g.attemptLogin("turing", "test")
    assert(usr == null)
  }
}
