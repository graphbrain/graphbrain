import org.scalatest.FunSuite
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.Node

class EdgeTest extends FunSuite {
	test("id generator") {
		val node0 = Node("node0")
		val node1 = Node("node1")
		val node2 = Node("node2")
		
		val edge = Edge("test", Array[Node](node0, node1, node2))

		assert(edge.id == "test node0 node1 node2")
	}

	test("participantIds") {
		val node0 = Node("node0")
		val node1 = Node("node1")
		val node2 = Node("node2")
		
		val edge = Edge("test", Array[Node](node0, node1, node2))

		val ids = edge.participantIds

		assert(ids == List[String]("node0", "node1", "node2"))
	}
}