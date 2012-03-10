import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.ImageNode
import com.graphbrain.hgdb.SourceNode

trait BaseVertexStoreTests { this: FunSuite =>

  def baseTests(store: VertexStore, label: String) {

  test("addrel with inexistant vertex [" + label + "]") {
    //store.addrel("test", Array("dummy1", "dummy2"))
  }  

  test("put/get Vertex [" + label + "]") {
    val vertex = TextNode("vertex0", "vertex0")
    store.remove(vertex)
    store.put(vertex)
    
    val vertexOut = store.get("vertex0")
    assert(vertex.id == vertexOut.id)
  }

  test("get Vertex that does not exist [" + label + "]") {
    val outVertex = store.get("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("get Edge that does not exist [" + label + "]") {
    val outVertex = store.getEdge("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("get EdgeType that does not exist [" + label + "]") {
    val outVertex = store.getEdgeType("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("get TextNode that does not exist [" + label + "]") {
    val outVertex = store.getTextNode("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("get URLNode that does not exist [" + label + "]") {
    val outVertex = store.getURLNode("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("get ImageNode that does not exist [" + label + "]") {
    val outVertex = store.getImageNode("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("get SourceNode that does not exist [" + label + "]") {
    val outVertex = store.getSourceNode("sdfh89g89gdf")
    assert(outVertex.id == "")
  }

  test("getTextNode [" + label + "]") {
    val inVertex = TextNode("textnode/0", "testing TextNode")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getTextNode("textnode/0")
    assert(outVertex.vtype == "txt")
    assert(inVertex.id == outVertex.id)
    assert(inVertex.text == outVertex.text)
  }

  test("getURLNode [" + label + "]") {
    val inVertex = URLNode("urlnode/0", "http://graphbrain.com")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getURLNode("urlnode/0")
    assert(outVertex.vtype == "url")
    assert(inVertex.id == outVertex.id)
    assert(inVertex.url == outVertex.url)
  }

  test("getImageNode [" + label + "]") {
    val inVertex = ImageNode("imagenode/0", "http://graphbrain.com/test.jpg")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getImageNode("imagenode/0")
    assert(outVertex.vtype == "img")
    assert(inVertex.id == outVertex.id)
    assert(inVertex.url == outVertex.url)
  }

  test("getSourceNode [" + label + "]") {
    val inVertex = SourceNode("sourcenode/0")
    store.remove(inVertex)
    store.put(inVertex)
    
    val outVertex = store.getSourceNode("sourcenode/0")
    assert(outVertex.vtype == "src")
    assert(inVertex.id == outVertex.id)
  }

  test("add two node relationship [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    store.addrel("test", Array[String]("node0", "node1"))

    val eid = "test " + node0.id + " " + node1.id
    val edge = store.get(eid)
    assert(edge.id == eid)
  }

  test("delete two node relationship [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    store.addrel("test", Array[String]("node0", "node1"))
    store.delrel("test", Array[String]("node0", "node1"))
  }

  test("two neighbors [" + label + "]") {
    val node0 = TextNode("node0", "node0")
    val node1 = TextNode("node1", "node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    store.addrel("test", Array[String]("node0", "node1"))

    assert(store.neighbors("node0", 2).toSet == Set[(String, String)](("node0", ""), ("node1", "node0")))
    assert(store.neighbors("node1", 2).toSet == Set[(String, String)](("node0", "node1"), ("node1", "")))
  }

  test("a few neighbors [" + label + "]") {
    val node0 = TextNode("node0", ""); store.remove(node0); store.put(node0)
    val node1 = TextNode("node1", ""); store.remove(node1); store.put(node1)
    val node2 = TextNode("node2", ""); store.remove(node2); store.put(node2)
    val node3 = TextNode("node3", ""); store.remove(node3); store.put(node3)
    val node4 = TextNode("node4", ""); store.remove(node4); store.put(node4)
    val node5 = TextNode("node5", ""); store.remove(node5); store.put(node5)
    val node6 = TextNode("node6", ""); store.remove(node6); store.put(node6)
    val node7 = TextNode("node7", ""); store.remove(node7); store.put(node7)
    
    store.addrel("test", Array[String]("node0", "node1"))
    store.addrel("test", Array[String]("node0", "node2"))
    store.addrel("test", Array[String]("node0", "node3"))
    store.addrel("test", Array[String]("node0", "node4", "node5"))
    store.addrel("test", Array[String]("node5", "node6"))
    store.addrel("test", Array[String]("node6", "node7"))

    assert(store.neighbors("node0", 0).toSet == Set[(String, String)](("node0", "")))
    assert(store.neighbors("node0", 1).toSet == Set[(String, String)](("node0", ""),
        ("node1", "node0"), ("node2", "node0"), ("node3", "node0"), ("node4", "node0"), ("node5", "node0")))
    assert(store.neighbors("node0", 2).toSet == Set[(String, String)](("node0", ""),
        ("node1", "node0"), ("node2", "node0"), ("node3", "node0"), ("node4", "node0"), ("node5", "node0"), ("node6", "node5")))
    assert(store.neighbors("node0", 3).toSet == Set[(String, String)](("node0", ""),
        ("node1", "node0"), ("node2", "node0"), ("node3", "node0"), ("node4", "node0"), ("node5", "node0"), ("node6", "node5"),
        ("node7", "node6")))
    assert(store.neighbors("node5", 2).toSet == Set[(String, String)](("node0", "node5"),
        ("node1", "node0"), ("node2", "node0"), ("node3", "node0"), ("node4", "node5"), ("node5", ""), ("node6", "node5"),
        ("node7", "node6")))
  }

  test("neighborEdges for two neighbors [" + label + "]") {
    val node0 = TextNode("node0", ""); store.remove(node0); store.put(node0)
    val node1 = TextNode("node1", ""); store.remove(node1); store.put(node1)

    store.addrel("test", Array[String]("node0", "node1"))

    val edges = store.neighborEdges(store.neighbors("node0"))
    assert(edges == Set[String]("test node0 node1"))
  }

  test("neighborEdges for a few neighbors [" + label + "]") {
    val node0 = TextNode("node0", ""); store.remove(node0); store.put(node0)
    val node1 = TextNode("node1", ""); store.remove(node1); store.put(node1)
    val node2 = TextNode("node2", ""); store.remove(node2); store.put(node2)
    val node3 = TextNode("node3", ""); store.remove(node3); store.put(node3)
    val node4 = TextNode("node4", ""); store.remove(node4); store.put(node4)
    val node5 = TextNode("node5", ""); store.remove(node5); store.put(node5)
    val node6 = TextNode("node6", ""); store.remove(node6); store.put(node6)
    val node7 = TextNode("node7", ""); store.remove(node7); store.put(node7)
    
    store.addrel("test", Array[String]("node0", "node1")); val e01 = "test node0 node1"
    store.addrel("test", Array[String]("node0", "node2")); val e02 = "test node0 node2"
    store.addrel("test", Array[String]("node0", "node3")); val e03 = "test node0 node3"
    store.addrel("test", Array[String]("node0", "node4", "node5")); val e045 = "test node0 node4 node5"
    store.addrel("test", Array[String]("node5", "node6")); val e56 = "test node5 node6"
    store.addrel("test", Array[String]("node6", "node7")); val e67 = "test node6 node7"

    assert(store.neighborEdges(store.neighbors("node0", 0)) == Set[String]())
    assert(store.neighborEdges(store.neighbors("node0", 1)) == Set[String](e01, e02, e03, e045))
    assert(store.neighborEdges(store.neighbors("node0", 2)) == Set[String](e01, e02, e03, e045, e56))
    assert(store.neighborEdges(store.neighbors("node0", 3)) == Set[String](e01, e02, e03, e045, e56, e67))
    assert(store.neighborEdges(store.neighbors("node5", 2)) == Set[String](e01, e02, e03, e045, e56, e67))
  }
  }
}