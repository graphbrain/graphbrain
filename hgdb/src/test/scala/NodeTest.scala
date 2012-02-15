import org.scalatest.FunSuite
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.Node

class NodeTest extends FunSuite {
	test("add Edge") {
		val node0 = Node("node0")
		val node1 = Node("node1")
		
		val edge = Edge("test", Array[String]("node0", "node1"))

		val node = node0.addEdge(edge)

		assert(node.edges == Set[String](edge.id))
	}
}