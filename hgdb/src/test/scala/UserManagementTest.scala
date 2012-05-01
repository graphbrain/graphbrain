import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.UserManagement


class UserManagementTest extends FunSuite {

  val store = new VertexStore("testhgdb", 10) with UserManagement

  test("create user") {
    store.createUser("testuser", "George Michael Bluth", "testuser@example.com", "test123_!", "user")
    val userNode = store.findUser("testuser")
    assert(userNode != null)
    assert(store.findUser("testuser@example.com") != null)
    assert(userNode.username == "testuser")
    assert(userNode.name == "George Michael Bluth")
    assert(userNode.email == "testuser@example.com")
  }

  test("try to find inexistent users") {
    assert(store.findUser("testuser2") == null)
    assert(store.findUser("testuser2@example.com") == null)
  }

  test("check password") {
    store.createUser("testuser", "George Michael Bluth", "testuser@example.com", "test123_!", "user")
    val userNode = store.findUser("testuser")
    assert(store.checkPassword(userNode, "test123_!"))
    assert(!store.checkPassword(userNode, "test123!"))
  }
}