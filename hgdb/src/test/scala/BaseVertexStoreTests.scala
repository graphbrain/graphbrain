import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Node

trait BaseVertexStoreTests { this: FunSuite =>

  def baseTests(store: VertexStore, label: String) {

  test("put Vertex [" + label + "]") {
    val vertex = Vertex("vertex0")
    store.remove(vertex)
    store.put(vertex)
    
    val vertexOut = store.get("vertex0")
    assert(vertex.id == vertexOut.id)
  }

  test("put Node [" + label + "]") {
    val node = Node("node0")
    store.remove(node)
    store.put(node)
    
    val nodeOut = store.get("node0")
    assert(node.id == nodeOut.id)
  }

  test("add two node relationship [" + label + "]") {
    val node0 = Node("node0")
    val node1 = Node("node1")
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
    val node0 = Node("node0")
    val node1 = Node("node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    store.addrel("test", Array[String]("node0", "node1"))
    store.delrel("test", Array[String]("node0", "node1"))
  }

  test("two neighbors [" + label + "]") {
    val node0 = Node("node0")
    val node1 = Node("node1")
    store.remove(node0)
    store.remove(node1)
    store.put(node0)
    store.put(node1)
    store.addrel("test", Array[String]("node0", "node1"))

    assert(store.neighbors("node0", 2).toSet == Set[String]("node0", "node1"))
    assert(store.neighbors("node1", 2).toSet == Set[String]("node0", "node1"))
  }

  test("a few neighbors [" + label + "]") {
    val node0 = Node("node0"); store.remove(node0); store.put(node0)
    val node1 = Node("node1"); store.remove(node1); store.put(node1)
    val node2 = Node("node2"); store.remove(node2); store.put(node2)
    val node3 = Node("node3"); store.remove(node3); store.put(node3)
    val node4 = Node("node4"); store.remove(node4); store.put(node4)
    val node5 = Node("node5"); store.remove(node5); store.put(node5)
    val node6 = Node("node6"); store.remove(node6); store.put(node6)
    val node7 = Node("node7"); store.remove(node7); store.put(node7)
    
    store.addrel("test", Array[String]("node0", "node1"))
    store.addrel("test", Array[String]("node0", "node2"))
    store.addrel("test", Array[String]("node0", "node3"))
    store.addrel("test", Array[String]("node0", "node4", "node5"))
    store.addrel("test", Array[String]("node5", "node6"))
    store.addrel("test", Array[String]("node6", "node7"))

    assert(store.neighbors("node0", 0).toSet == Set[String]("node0"))
    assert(store.neighbors("node0", 1).toSet == Set[String]("node0", "node1", "node2", "node3", "node4", "node5"))
    assert(store.neighbors("node0", 2).toSet == Set[String]("node0", "node1", "node2", "node3", "node4", "node5", "node6"))
    assert(store.neighbors("node0", 3).toSet == Set[String]("node0", "node1", "node2", "node3", "node4", "node5", "node6", "node7"))
    assert(store.neighbors("node5", 2).toSet == Set[String]("node0", "node1", "node2", "node3", "node4", "node5", "node6", "node7"))
  }

  test("neighborEdges for two neighbors [" + label + "]") {
    val node0 = Node("node0"); store.remove(node0); store.put(node0)
    val node1 = Node("node1"); store.remove(node1); store.put(node1)

    store.addrel("test", Array[String]("node0", "node1"))

    val edges = store.neighborEdges(store.neighbors("node0"))
    assert(edges == Set[String]("test node0 node1"))
  }

  test("neighborEdges for a few neighbors [" + label + "]") {
    val node0 = Node("node0"); store.remove(node0); store.put(node0)
    val node1 = Node("node1"); store.remove(node1); store.put(node1)
    val node2 = Node("node2"); store.remove(node2); store.put(node2)
    val node3 = Node("node3"); store.remove(node3); store.put(node3)
    val node4 = Node("node4"); store.remove(node4); store.put(node4)
    val node5 = Node("node5"); store.remove(node5); store.put(node5)
    val node6 = Node("node6"); store.remove(node6); store.put(node6)
    val node7 = Node("node7"); store.remove(node7); store.put(node7)
    
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