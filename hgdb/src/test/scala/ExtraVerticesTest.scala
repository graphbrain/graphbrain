import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.TextNode


class ExtraVerticesTest extends FunSuite {
  val store = new VertexStore("testhgdb", 10)

  test("100 edges on a 10 edge limit per vertex") {
    val node0 = TextNode("node0", "?")
    store.remove(node0)
    store.put(node0)
    for (n <- 1 to 100) {
      store.remove(TextNode("node" + n, "?"))
      store.put(TextNode("node" + n, "?"))
      store.addrel("test", Array("node0", "node" + n))
    }
    val newNode0 = store.getTextNode("node0")
    assert(newNode0.extra == 9)
    assert(newNode0.edges.size == 10)
  }
}