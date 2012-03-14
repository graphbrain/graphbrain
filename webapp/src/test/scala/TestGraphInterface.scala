import org.scalatest.FunSuite
import com.graphbrain.webapp.GraphInterface
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.hgdb.Edge
import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{Map => MMap}

class TestGraphInterface extends FunSuite {
  /*
  def createGraph(store: VertexStore) = {
    val a = Node("a"); store.update(a)
    val b = Node("b"); store.update(b)
    val c = Node("c"); store.update(c)
    val d = Node("d"); store.update(d)
    val e = Node("e"); store.update(e)
    val f = Node("f"); store.update(f)
    val g = Node("g"); store.update(g)
    val h = Node("h"); store.update(h)
    
    store.addrel("is", Array[String]("a", "b"))
    store.addrel("is", Array[String]("a", "c"))
    store.addrel("is", Array[String]("a", "d"))
    store.addrel("is", Array[String]("a", "e"))
    store.addrel("is", Array[String]("f", "b"))
    store.addrel("is", Array[String]("g", "b"))
  }

  test("super nodes") {
    val store = new VertexStore("testhgdb") with SimpleCaching
    createGraph(store)
    val gi = new GraphInterface("a", store)

    val expected = Set(Map("id" -> "sn0", "key" -> ("", -1, ""), "nodes" -> Set("a")),
      Map("id" -> "sn1", "key" -> ("is", 0, "a"), "nodes" -> Set("c", "d", "e", "b")),
      Map("id" -> "sn2", "key" -> ("is", 1, "b"), "nodes" -> Set("g", "f")))
    assert(gi.snodes == expected)
  }

  test("visual links") {
    val store = new VertexStore("testhgdb") with SimpleCaching
    createGraph(store)
    val gi = new GraphInterface("a", store)

    val expected = Set(("is", "a", "sn1"), ("is", "sn2", "b"))
    assert(gi.links == expected)
  }*/
}