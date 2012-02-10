import org.scalatest.FunSuite
import com.graphbrain.HGDB
import com.graphbrain.Vertex
import com.graphbrain.Node

class HGDBTest extends FunSuite {
  val hgdb = HGDB("testhgdb.testcoll")

  test("put Vertex") {
    val vertex = Vertex("vertex0")
    hgdb.remove(vertex)
    hgdb.put(vertex)
    
    val vertexOut = hgdb.get("vertex0")
    assert(vertex._id == vertexOut._id)
  }

  test("put Node") {
    val node = Node("node0")
    hgdb.remove(node)
    hgdb.put(node)
    
    val nodeOut = hgdb.get("node0")
    assert(node._id == nodeOut._id)
  }

  test("add two node relationship") {
    val node0 = Node("node0")
    val node1 = Node("node1")
    hgdb.remove(node0)
    hgdb.remove(node1)
    hgdb.put(node0)
    hgdb.put(node1)
    hgdb.addrel("test", Array[Node](node0, node1))

    val eid = "test " + node0._id + " " + node1._id
    val edge = hgdb.get(eid)
    assert(edge._id == eid)
  }

  test("delete two node relationship") {
    val node0 = Node("node0")
    val node1 = Node("node1")
    hgdb.remove(node0)
    hgdb.remove(node1)
    hgdb.put(node0)
    hgdb.put(node1)
    hgdb.addrel("test", Array[Node](node0, node1))
    hgdb.delrel("test", Array[Node](node0, node1))
  }
}