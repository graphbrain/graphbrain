import org.scalatest.FunSuite
import com.graphbrain.gbdb.VertexStore
import com.graphbrain.gbdb.SimpleCaching

class SimpleCachingTest extends FunSuite with BaseVertexStoreTests{
  val store = new VertexStore("gb", "testhgdb") with SimpleCaching
  store.simpleLogOn = false

  testsFor(baseTests(store, "VertexStore with SimpleCaching"))
}
