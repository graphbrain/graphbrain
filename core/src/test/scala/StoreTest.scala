import org.scalatest.FunSuite
import com.graphbrain.Store

class StoreTest extends FunSuite {
	val store = Store("testdb.testcoll")

	test("put Map with a few Strings") {
		store.remove("test0")
		store.put(Map[String, Any](("_id" -> "test0"), ("a" -> "b"), ("c" -> "d")))
		
		val map = store.get("test0")
		assert(map == Map[String, Any](("_id" -> "test0"), ("a" -> "b"), ("c" -> "d")))
	}

	test("update Map with a few Strings") {
		store.remove("test0")
		store.put(Map[String, Any](("_id" -> "test0"), ("a" -> "b"), ("c" -> "d")))

		store.update("test0", Map[String, Any](("_id" -> "test0"), ("a" -> "new")))
		val map = store.get("test0")
		assert(map == Map[String, Any](("_id" -> "test0"), ("a" -> "new")))
	}

	test("remove Map") {
		store.remove("test0")
		store.put(Map[String, Any](("_id" -> "test0"), ("a" -> "b"), ("c" -> "d")))

		store.remove("test0")
		
		val map = store.get("test0")
		assert(map == Map[String, Any]())
	}
}