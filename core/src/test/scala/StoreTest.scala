import org.scalatest.FunSuite
import com.graphbrain.Store

class StoreTest extends FunSuite {
	val store = Store("testdb", "testcoll")

	test("put Map with a few Strings") {
		store.put(Map[String, Any](("_id" -> "test0"), ("a" -> "b"), ("c" -> "d")))
	}

	test("get Map with a few Strings") {
		val map = store.get("test0")
		assert(map == Map[String, Any](("_id" -> "test0"), ("a" -> "b"), ("c" -> "d")))
	}

	test("update Map with a few Strings") {
		store.update("test0", Map[String, Any](("_id" -> "test0"), ("a" -> "new")))
		val map = store.get("test0")
		assert(map == Map[String, Any](("_id" -> "test0"), ("a" -> "new")))
	}
}