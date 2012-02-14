import org.scalatest.FunSuite
import com.graphbrain.Edge
import com.graphbrain.Node

class NodeTest extends FunSuite {
	test("add Edge") {
		val node0 = Node("node0")
		val node1 = Node("node1")
		
		val edge = Edge("test", Array[Node](node0, node1))

		val node = node0.addEdge(edge)

		assert(node.edges == Set[String](edge.id))
	}
}