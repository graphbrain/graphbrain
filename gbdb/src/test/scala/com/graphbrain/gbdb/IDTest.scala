import org.scalatest.FunSuite
import com.graphbrain.gbdb.ID


class IDTest extends FunSuite {
  test("is in user space") {
    assert(ID.isInUserSpace("user/dummy/1/test"))
    assert(!ID.isInUserSpace("user/dummy"))
    assert(ID.isInUserSpace("user/dummy/test"))
    assert(!ID.isInUserSpace("1/test"))
    assert(!ID.isInUserSpace("test"))
  }

  test("is a user node") {
    assert(!ID.isUserNode("user/dummy/1/test"))
    assert(ID.isUserNode("user/dummy"))
    assert(!ID.isUserNode("1/test"))
  }

  test("global to user") {
    assert(ID.globalToUser("1/test", "user/dummy") == "user/dummy/1/test")
    assert(ID.globalToUser("user/dummy/1/test", "user/dummy") == "user/dummy/1/test")
    assert(ID.globalToUser("user/dummy", "user/dummy") == "user/dummy")
  }

  test("user to global") {
    assert(ID.userToGlobal("1/test") == "1/test")
    assert(ID.userToGlobal("user/dummy/1/test") == "1/test")
    assert(ID.userToGlobal("user/dummy") == "user/dummy")
  }

  test("is personal") {
    assert(ID.isPersonal("user/dummy/p/1/test"))
    assert(!ID.isPersonal("user/dummy/1/test"))
    assert(!ID.isPersonal("user/dummy"))
    assert(!ID.isPersonal("user/dummy/test"))
    assert(!ID.isPersonal("1/test"))
    assert(!ID.isPersonal("test"))
  }

  test("is in user global space") {
    assert(!ID.isInUserGlobalSpace("user/dummy/p/1/test"))
    assert(ID.isInUserGlobalSpace("user/dummy/1/test"))
    assert(!ID.isInUserGlobalSpace("user/dummy"))
    assert(ID.isInUserGlobalSpace("user/dummy/test"))
    assert(!ID.isInUserGlobalSpace("1/test"))
    assert(!ID.isInUserGlobalSpace("test"))
  }
}