import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.KeyNotFound
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.SourceNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.ID

trait BaseVertexStoreTests { this: FunSuite =>

  def baseTests(store: VertexStore, label: String) {  

  test("put/get Vertex [" + label + "]") {
    val vertex = TextNode("vertex0", "vertex0")
    store.remove(vertex)
    store.put(vertex)
    
    val vertexOut = store.get("vertex0/vertex0")
    assert(vertex.id == vertexOut.id)
    assert(store.exists("vertex0/vertex0"))
  }

  test("get Vertex that does not exist [" + label + "]") {
    intercept[KeyNotFound] {
      store.get("sdfh89g89gdf")
    }
  }

  test("get EdgeType that does not exist [" + label + "]") {
    intercept[KeyNotFound] {
      store.getEdgeType("sdfh89g89gdf")
    }
  }

  test("get TextNode that does not exist [" + label + "]") {
    intercept[KeyNotFound] {
      store.getTextNode("sdfh89g89gdf")
    }
  }

  test("get SourceNode that does not exist [" + label + "]") {
    intercept[KeyNotFound] {
      store.getSourceNode("sdfh89g89gdf")
    }
  }

  test("get UserNode that does not exist [" + label + "]") {
    intercept[KeyNotFound] {
      store.getUserNode("sdfh89g89gdf")
    }
  }

  test("getTextNode [" + label + "]") {
    val inVertex = TextNode("textnode", "testing TextNode")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getTextNode("textnode/testing_textnode")
    assert(inVertex.id == outVertex.id)
    assert(inVertex.text == outVertex.text)
    assert(store.exists("textnode/testing_textnode"))
  }

  test("getURLNode [" + label + "]") {
    val inVertex = URLNode("url/xxx", "http://graphbrain.com")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getURLNode("url/xxx")
    assert(inVertex.id == outVertex.id)
    assert(inVertex.url == outVertex.url)
    assert(store.exists("url/xxx"))
  }

  test("getUserNode [" + label + "]") {
    val inVertex = UserNode("user/username")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getUserNode("user/username")
    assert(inVertex.id == outVertex.id)
    assert(store.exists("user/username"))
  }

  test("add two node relationship [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)

    val edge = store.addrel("test", List[String]("node0/node0", "node1/node1"))

    assert(store.relExistsOnVertex("node0/node0", edge))
    assert(store.relExistsOnVertex("node1/node1", edge))
    assert(store.relExists(edge))
  }

  test("add relationship twice [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)

    val edge = store.addrel("test", List[String]("node0/node0", "node1/node1"))
    store.addrel("test", List[String]("node0/node0", "node1/node1"))

    assert(store.relExistsOnVertex("node0/node0", edge))
    assert(store.relExistsOnVertex("node1/node1", edge))
    assert(store.relExists(edge))
  }

  test("delete two node relationship [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    val edge = store.addrel("test", List[String]("node0/node0", "node1/node1"))
    store.delrel("test", List[String]("node0/node0", "node1/node1"))

    assert(!store.relExistsOnVertex("node0/node0", edge))
    assert(!store.relExistsOnVertex("node1/node1", edge))
    assert(!store.relExists(edge))
  }

  test("neighborEdges for two neighbors [" + label + "]") {
    val node0 = TextNode("node0", "?"); store.remove(node0); store.put(node0)
    val node1 = TextNode("node1", "?"); store.remove(node1); store.put(node1)

    store.addrel("test", List[String]("node0/?", "node1/?"))

    val edges = store.neighborEdges("node0/?")
    assert(edges == Set[Edge](Edge("test", List("node0/?", "node1/?"))))
  }

  test("neighborEdges for a few neighbors [" + label + "]") {
    val node0 = TextNode("node0", "?"); store.remove(node0); store.put(node0)
    val node1 = TextNode("node1", "?"); store.remove(node1); store.put(node1)
    val node2 = TextNode("node2", "?"); store.remove(node2); store.put(node2)
    val node3 = TextNode("node3", "?"); store.remove(node3); store.put(node3)
    val node4 = TextNode("node4", "?"); store.remove(node4); store.put(node4)
    val node5 = TextNode("node5", "?"); store.remove(node5); store.put(node5)
    val node6 = TextNode("node6", "?"); store.remove(node6); store.put(node6)
    val node7 = TextNode("node7", "?"); store.remove(node7); store.put(node7)
    
    val e01 = store.addrel("test", List[String]("node0/?", "node1/?"))
    val e02 = store.addrel("test", List[String]("node0/?", "node2/?"))
    val e03 = store.addrel("test", List[String]("node0/?", "node3/?"))
    val e045 = store.addrel("test", List[String]("node0/?", "node4/?", "node5/?"))
    val e56 = store.addrel("test", List[String]("node5/?", "node6/?"))
    val e67 = store.addrel("test", List[String]("node6/?", "node7/?"))

    assert(store.neighborEdges("node0/?") == Set[Edge](e01, e02, e03, e045))
    assert(store.neighborEdges("node1/?") == Set[Edge](e01))
    assert(store.neighborEdges("node2/?") == Set[Edge](e02))
    assert(store.neighborEdges("node3/?") == Set[Edge](e03))
    assert(store.neighborEdges("node4/?") == Set[Edge](e045))
    assert(store.neighborEdges("node5/?") == Set[Edge](e045, e56))
    assert(store.neighborEdges("node6/?") == Set[Edge](e56, e67))
    assert(store.neighborEdges("node7/?") == Set[Edge](e67))
  }

  test("two neighbors [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    store.addrel("test", List[String]("node0/node0", "node1/node1"))

    assert(store.neighbors("node0/node0") == Set[String]("node0/node0", "node1/node1"))
    assert(store.neighbors("node1/node1") == Set[String]("node0/node0", "node1/node1"))
  }

  test("a few neighbors [" + label + "]") {
    val node0 = TextNode("node0", "?"); store.remove(node0); store.put(node0)
    val node1 = TextNode("node1", "?"); store.remove(node1); store.put(node1)
    val node2 = TextNode("node2", "?"); store.remove(node2); store.put(node2)
    val node3 = TextNode("node3", "?"); store.remove(node3); store.put(node3)
    val node4 = TextNode("node4", "?"); store.remove(node4); store.put(node4)
    val node5 = TextNode("node5", "?"); store.remove(node5); store.put(node5)
    val node6 = TextNode("node6", "?"); store.remove(node6); store.put(node6)
    val node7 = TextNode("node7", "?"); store.remove(node7); store.put(node7)
    
    store.addrel("test", List[String]("node0/?", "node1/?"))
    store.addrel("test", List[String]("node0/?", "node2/?"))
    store.addrel("test", List[String]("node0/?", "node3/?"))
    store.addrel("test", List[String]("node0/?", "node4/?", "node5/?"))
    store.addrel("test", List[String]("node5/?", "node6/?"))
    store.addrel("test", List[String]("node6/?", "node7/?"))

    assert(store.neighbors("node0/?").toSet == Set[String]("node0/?",
        "node1/?", "node2/?", "node3/?", "node4/?", "node5/?"))
    assert(store.neighbors("node1/?").toSet == Set[String]("node0/?", "node1/?"))
    assert(store.neighbors("node2/?").toSet == Set[String]("node0/?", "node2/?"))
    assert(store.neighbors("node3/?").toSet == Set[String]("node0/?", "node3/?"))
    assert(store.neighbors("node4/?").toSet == Set[String]("node0/?", "node4/?", "node5/?"))
    assert(store.neighbors("node5/?").toSet == Set[String]("node0/?", "node4/?", "node5/?", "node6/?"))
    assert(store.neighbors("node6/?").toSet == Set[String]("node5/?", "node6/?", "node7/?"))
    assert(store.neighbors("node7/?").toSet == Set[String]("node7/?", "node6/?"))
  }
  }

}
