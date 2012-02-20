import org.scalatest.FunSuite
import com.graphbrain.hgdb.VertexStore

class VertexStoreTest extends FunSuite with BaseVertexStoreTests{
  val store = new VertexStore("testhgdb")

  testsFor(baseTests(store, "VertexStore"))
}