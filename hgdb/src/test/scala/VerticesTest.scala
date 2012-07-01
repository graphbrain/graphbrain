import org.scalatest.FunSuite
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode


class VerticesTest extends FunSuite {
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
}

