import org.scalatest.FunSuite
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.TextNode

class NodeTest extends FunSuite {
	test("add Edge") {
		val node0 = TextNode("node0", "")
		val node1 = TextNode("node1", "")
		
		val edge = new Edge("test", Array[String]("node0", "node1"))

		val node = node0.addEdge(edge.id)

		assert(node.edges == Set[String](edge.id))
		assert(node.edges == Set[String]("test node0 node1"))
	}

	test("add two Edge") {
		val node0 = TextNode("node0", "")
		val node1 = TextNode("node1", "")
		val node2 = TextNode("node2", "")
		
		val edge01 = new Edge("test", Array[String]("node0", "node1"))
		val edge02 = new Edge("test", Array[String]("node0", "node2"))

		val node = node0.addEdge(edge01.id).addEdge(edge02.id)

		assert(node.edges == Set[String](edge01.id, edge02.id))
	}

	test("delete Edge") {
		val node0 = TextNode("node0", "")
		val node1 = TextNode("node1", "")
		val node2 = TextNode("node2", "")
		
		val edge01 = new Edge("test", Array[String]("node0", "node1"))
		val edge02 = new Edge("test", Array[String]("node0", "node2"))

		val node = node0.addEdge(edge01.id).addEdge(edge02.id)
		val nodeDel1 = node.delEdge(edge01.id)
		val nodeDel2 = node.delEdge(edge02.id)
		val nodeDel12 = node.delEdge(edge01.id).delEdge(edge02.id)
		assert(nodeDel1.edges == Set[String](edge02.id))
		assert(nodeDel2.edges == Set[String](edge01.id))
		assert(nodeDel12.edges == Set[String]())
	}
}