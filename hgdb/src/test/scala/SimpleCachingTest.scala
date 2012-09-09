import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching

class SimpleCachingTest extends FunSuite with BaseVertexStoreTests{
  val store = new VertexStore("gb", "testhgdb") with SimpleCaching

  testsFor(baseTests(store, "VertexStore with SimpleCaching"))
}
