import org.scalatest.FunSuite
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode


class VerticesTest extends FunSuite {
  /*
  test("Edge id generator") {
  val edge = new Edge("test", Array[String]("node0", "node1", "node2"))
  assert(edge.id == "test node0 node1 node2")
  }

  test("Edge participantIds") {
  val edge = new Edge("test", Array[String]("node0", "node1", "node2"))
  val ids = edge.participantIds
  assert(ids == List[String]("node0", "node1", "node2"))
  }

  test("edgeType") {
    val edge = new Edge("test", Array[String]("node0", "node1", "node2"))
    assert(edge.etype == "test")
  }

  test("add edge") {
    val node0 = TextNode("node0", "")
    
    val edge = new Edge("test", Array[String]("node0", "node1"))

    val node = node0.setEdges(node0.edges + edge.id)

    assert(node.edges == Set[String](edge.id))
    assert(node.edges == Set[String]("test node0 node1"))
  }*/
}

