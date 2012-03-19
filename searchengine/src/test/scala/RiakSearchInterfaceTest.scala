import org.scalatest.FunSuite
import com.graphbrain.searchengine.RiakSearchInterface


class RiakSearchInterfaceTest extends FunSuite {
	val rsi = RiakSearchInterface("testsearch")

	test("indexing a few documents and searching") {
		rsi.initIndex()
    	rsi.index("searchtest_1", "GraphBrain is searchable")
    	rsi.index("searchtest_2", "Wikipedia is searchable")
    	println(rsi.query("searchable"))
	}
}