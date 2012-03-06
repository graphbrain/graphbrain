import org.scalatest.FunSuite
import com.graphbrain.hgdb.Edge


class EdgeTest extends FunSuite {
	test("id generator") {
		val edge = new Edge("test", Array[String]("node0", "node1", "node2"))
		assert(edge.id == "test node0 node1 node2")
	}

	test("participantIds") {
		val edge = new Edge("test", Array[String]("node0", "node1", "node2"))
		val ids = edge.participantIds
		assert(ids == List[String]("node0", "node1", "node2"))
	}

  test("edgeType") {
    val edge = new Edge("test", Array[String]("node0", "node1", "node2"))
    assert(edge.etype == "test")
  }
}