import org.scalatest.FunSuite
import com.graphbrain.searchengine.RiakSearchInterface


class RiakSearchInterfaceTest extends FunSuite {
	val rsi = RiakSearchInterface("testsearch")

	test("indexing a few documents and searching") {
		rsi.initIndex()
    	rsi.index("searchtest/1", "GraphBrain is searchable")
    	rsi.index("searchtest/2", "Wikipedia is searchable")
    	rsi.index("searchtest/3", "Herp Derp")
    	val results = rsi.query("searchable")
		assert(results.numResults >= 2)
		assert(results.ids.contains("searchtest/1"))
		assert(results.ids.contains("searchtest/2"))
		assert(!results.ids.contains("searchtest/3"))
	}
}