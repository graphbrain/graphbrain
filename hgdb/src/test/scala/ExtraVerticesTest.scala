import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.TextNode


class ExtraVerticesTest extends FunSuite {
/*
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
    assert(newNode0.edges.contains("test node0 node1"))
    assert(newNode0.edges.contains("test node0 node10"))
    assert(!newNode0.edges.contains("test node0 node11"))

    val extra1 = store.getExtraEdges("node0/1")
    assert(extra1.id != "")
    assert(!extra1.edges.contains("test node0 node10"))
    assert(extra1.edges.contains("test node0 node11"))
    assert(extra1.edges.contains("test node0 node20"))
    assert(!extra1.edges.contains("test node0 node21"))

    val extra9 = store.getExtraEdges("node0/9")
    assert(extra9.id != "")
    assert(!extra9.edges.contains("test node0 node90"))
    assert(extra9.edges.contains("test node0 node91"))
    assert(extra9.edges.contains("test node0 node100"))

    val extra10 = store.getExtraEdges("node0/10")
    assert(extra10.id == "")
  }

  test("delrel with extra vertices") {
    val node0 = TextNode("node0", "?")
    store.remove(node0)
    store.put(node0)
    for (n <- 1 to 100) {
      store.remove(TextNode("node" + n, "?"))
      store.put(TextNode("node" + n, "?"))
      store.addrel("test", Array("node0", "node" + n))
    }

    store.delrel("test", Array("node0", "node100"))
    val extra9 = store.getExtraEdges("node0/9")
    assert(!extra9.edges.contains("test node0 node100"))
    assert(store.get("node0").extra == 9)

    store.delrel("test", Array("node0", "node11"))
    assert(!store.getExtraEdges("node0/1").edges.contains("test node0 node11"))
    assert(store.get("node0").extra == 1)

    store.put(TextNode("node101", "?"))
    store.addrel("test", Array("node0", "node101"))
    assert(store.getExtraEdges("node0/1").edges.contains("test node0 node101"))
    assert(store.get("node0").extra == 1)

    store.put(TextNode("node102", "?"))
    store.addrel("test", Array("node0", "node102"))
    assert(store.getExtraEdges("node0/9").edges.contains("test node0 node102"))
    assert(store.get("node0").extra == 9)

    store.put(TextNode("node103", "?"))
    store.addrel("test", Array("node0", "node103"))
    assert(store.getExtraEdges("node0/10").edges.contains("test node0 node103"))
    assert(store.get("node0").extra == 10)

    store.delrel("test", Array("node0", "node1"))
    assert(!store.get("node0").edges.contains("test node0 node1"))
    assert(store.get("node0").extra == 0)
  }*/
}