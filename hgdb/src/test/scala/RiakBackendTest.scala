import org.scalatest.FunSuite
import com.graphbrain.hgdb.RiakBackend

class MapStoreTest extends FunSuite {
	val backend = new RiakBackend("testdb")

	test("put Map with a few Strings") {
		backend.remove("test0")
		backend.put("test0", Map[String, Any](("id" -> "test0"), ("abcde" -> "edcba"), ("c" -> "d|e")))
		
		val map = backend.get("test0")
		assert(map == Map[String, Any](("id" -> "test0"), ("abcde" -> "edcba"), ("c" -> "d|e")))
	}

	test("update Map with a few Strings") {
		backend.remove("test0")
		backend.put("test0", Map[String, Any](("id" -> "test0"), ("a" -> "b"), ("c" -> "d")))

		backend.update("test0", Map[String, Any](("id" -> "test0"), ("a" -> "new")))
		val map = backend.get("test0")
		assert(map == Map[String, Any](("id" -> "test0"), ("a" -> "new")))
	}

	test("remove Map") {
		backend.remove("test0")
		backend.put("test0", Map[String, Any](("id" -> "test0"), ("a" -> "b"), ("c" -> "d")))

		backend.remove("test0")
		
		val map = backend.get("test0")
		assert(map == Map[String, Any]())
	}
}