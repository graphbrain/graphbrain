import org.scalatest.FunSuite
import com.graphbrain.MapStore

class MapStoreTest extends FunSuite {
	val store = MapStore("testdb")

	test("put Map with a few Strings") {
		store.remove("test0")
		store.put("test0", Map[String, Any](("id" -> "test0"), ("abcde" -> "edcba"), ("c" -> "d|e")))
		
		val map = store.get("test0")
		assert(map == Map[String, Any](("id" -> "test0"), ("abcde" -> "edcba"), ("c" -> "d|e")))
	}

	test("update Map with a few Strings") {
		store.remove("test0")
		store.put("test0", Map[String, Any](("id" -> "test0"), ("a" -> "b"), ("c" -> "d")))

		store.update("test0", Map[String, Any](("id" -> "test0"), ("a" -> "new")))
		val map = store.get("test0")
		assert(map == Map[String, Any](("id" -> "test0"), ("a" -> "new")))
	}

	test("remove Map") {
		store.remove("test0")
		store.put("test0", Map[String, Any](("id" -> "test0"), ("a" -> "b"), ("c" -> "d")))

		store.remove("test0")
		
		val map = store.get("test0")
		assert(map == Map[String, Any]())
	}
}