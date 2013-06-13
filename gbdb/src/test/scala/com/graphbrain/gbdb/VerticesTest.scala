import org.scalatest.FunSuite
import com.graphbrain.gbdb.Edge
import com.graphbrain.gbdb.TextNode


class VerticesTest extends FunSuite {
  test("Edge id generator") {
  val edge = new Edge("test", List[String]("node0", "node1", "node2"))
  assert(edge.edgeType == "test")
  assert(edge.participantIds(0) == "node0")
  assert(edge.participantIds(1) == "node1")
  assert(edge.participantIds(2) == "node2")
  }

  test("Edge participantIds") {
  val edge = new Edge("test", List[String]("node0", "node1", "node2"))
  val ids = edge.participantIds
  assert(ids == List[String]("node0", "node1", "node2"))
  }

  test("edgeType") {
    val edge = new Edge("test", List[String]("node0", "node1", "node2"))
    assert(edge.edgeType == "test")
  }
}

