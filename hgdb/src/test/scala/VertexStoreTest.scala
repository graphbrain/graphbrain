import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore

class VertexStoreTest extends FunSuite with BaseVertexStoreTests{
  val store = new VertexStore("gb", "testhgdb")
  store.simpleLogOn = false

  testsFor(baseTests(store, "VertexStore"))
}
