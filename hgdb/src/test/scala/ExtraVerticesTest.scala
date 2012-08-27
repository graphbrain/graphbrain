import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.KeyNotFound
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.EdgeSet
import com.graphbrain.hgdb.ID


class ExtraVerticesTest extends FunSuite {

  val store = new VertexStore("testhgdb", 10)

  test("100 edges on a 10 edge limit per vertex") {
    val node0 = TextNode("n0", "?")
    store.remove(node0)
    store.put(node0)
    for (n <- 1 to 100) {
      store.remove(TextNode("n" + n, "?"))
      store.put(TextNode("n" + n, "?"))
      store.addrel("test", Array("n0/?", "n" + n + "/?"))
    }

    var edgeSetId = ID.edgeSetId(node0.id, "test n0/? n1/?")
    val node0EdgeSet = store.getEdgeSet(edgeSetId)
    assert(node0EdgeSet.extra == 9)
    assert(node0EdgeSet.edges.size == 10)
    assert(node0EdgeSet.edges.contains("test n0/? n1/?"))
    assert(node0EdgeSet.edges.contains("test n0/? n10/?"))
    assert(!node0EdgeSet.edges.contains("test n0/? n11/?"))

    val extra1 = store.getExtraEdges(edgeSetId + "/1")
    assert(extra1.id != "")
    assert(!extra1.edges.contains("test n0/? n10/?"))
    assert(extra1.edges.contains("test n0/? n11/?"))
    assert(extra1.edges.contains("test n0/? n20/?"))
    assert(!extra1.edges.contains("test n0/? n21/?"))

    val extra9 = store.getExtraEdges(edgeSetId + "/9")
    assert(extra9.id != "")
    assert(!extra9.edges.contains("test n0/? n90/?"))
    assert(extra9.edges.contains("test n0/? n91/?"))
    assert(extra9.edges.contains("test n0/? n100/?"))

    intercept[KeyNotFound] {
        store.getExtraEdges(edgeSetId + "/10")
    }
  }

  test("delrel with extra vertices") {
    val node0 = TextNode("node0", "?")
    store.remove(node0)
    store.put(node0)
    for (n <- 1 to 100) {
      store.remove(TextNode("node" + n, "?"))
      store.put(TextNode("node" + n, "?"))
      store.addrel("test", Array("node0/?", "node" + n + "/?"))
    }

    var edgeSetId = ID.edgeSetId(node0.id, "test node0/? node1/?")

    store.delrel("test", Array("node0/?", "node100/?"))
    val extra9 = store.getExtraEdges(edgeSetId + "/9")
    assert(!extra9.edges.contains("test node0/? node100/?"))
    assert(store.getEdgeSet(edgeSetId).extra == 9)

    store.delrel("test", Array("node0/?", "node11/?"))
    assert(!store.getExtraEdges(edgeSetId + "/1").edges.contains("test node0/? node11/?"))
    assert(store.getEdgeSet(edgeSetId).extra == 1)

    store.put(TextNode("node101", "?"))
    store.addrel("test", Array("node0/?", "node101/?"))
    assert(store.getExtraEdges(edgeSetId + "/1").edges.contains("test node0/? node101/?"))
    assert(store.getEdgeSet(edgeSetId).extra == 1)

    store.put(TextNode("node102", "?"))
    store.addrel("test", Array("node0/?", "node102/?"))
    assert(store.getExtraEdges(edgeSetId + "/9").edges.contains("test node0/? node102/?"))
    assert(store.getEdgeSet(edgeSetId).extra == 9)

    store.put(TextNode("node103", "?"))
    store.addrel("test", Array("node0/?", "node103/?"))
    assert(store.getExtraEdges(edgeSetId + "/10").edges.contains("test node0/? node103/?"))
    assert(store.getEdgeSet(edgeSetId).extra == 10)

    store.delrel("test", Array("node0/?", "node1/?"))
    assert(!store.getEdgeSet(edgeSetId).edges.contains("test node0/? node1/?"))
    assert(store.getEdgeSet(edgeSetId).extra == 0)
  }
}
