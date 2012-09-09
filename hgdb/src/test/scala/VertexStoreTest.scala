import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore

class VertexStoreTest extends FunSuite with BaseVertexStoreTests{
  val store = new VertexStore("gb", "testhgdb")

  testsFor(baseTests(store, "VertexStore"))
}
