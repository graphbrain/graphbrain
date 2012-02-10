import org.scalatest.FunSuite
import com.graphbrain.HGDB
import com.graphbrain.Vertex

class HGDBTest extends FunSuite {
	val hgdb = HGDB("testhgdb.testcoll")

	test("put Vertex") {
		val vertex = Vertex("vertex0")
		hgdb.remove(vertex)
		hgdb.put(vertex)
		
		val vertexOut = hgdb.get("vertex0")
		assert(vertex._id == vertexOut._id)
	}
}